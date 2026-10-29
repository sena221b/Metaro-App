package com.kampplus.hava.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.AirportRepository
import com.kampplus.hava.data.MapWeatherRepository
import com.kampplus.hava.model.AirportNode
import com.kampplus.hava.model.MapCoordinate
import com.kampplus.hava.model.MapForecast
import com.kampplus.hava.model.MapWeatherMarker
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class MapRoute(val departure: AirportNode, val arrival: AirportNode, val revision: Long)
data class MapSelection(val title: String, val point: MapCoordinate, val airportCode: String? = null,
    val forecast: MapForecast? = null, val loading: Boolean = false, val error: String? = null)
data class MapUiState(
    val route: MapRoute? = null,
    val departure: MapSelection? = null,
    val arrival: MapSelection? = null,
    val selected: MapSelection? = null,
    val routeLoading: Boolean = false,
    val routeError: String? = null,
    val markers: List<MapWeatherMarker> = emptyList(),
    val markerError: String? = null
)

class MapViewModel : ViewModel() {
    private val airports = AirportRepository()
    private val weather = MapWeatherRepository()
    private val mutableState = MutableStateFlow(MapUiState())
    val state = mutableState.asStateFlow()
    private var routeJob: Job? = null
    private var selectionJob: Job? = null
    private var markerJob: Job? = null
    private var markerKey: String? = null
    private val markerCache = LinkedHashMap<String, Pair<Long, List<MapWeatherMarker>>>()

    fun route(departure: String, arrival: String) {
        routeJob?.cancel()
        selectionJob?.cancel()
        mutableState.update { it.copy(routeLoading = true, routeError = null, route = null, departure = null, arrival = null, selected = null) }
        routeJob = viewModelScope.launch {
            try {
                val dep = async { airports.resolve(departure) }
                val arr = async { airports.resolve(arrival) }
                val route = MapRoute(dep.await(), arr.await(), System.nanoTime())
                val depSelection = route.departure.selection()
                val arrSelection = route.arrival.selection()
                mutableState.update { it.copy(route = route, departure = depSelection, arrival = arrSelection, selected = arrSelection, routeLoading = false) }
                val departureWeather = async { load(depSelection) }
                val arrivalWeather = async { load(arrSelection) }
                val first = departureWeather.await()
                val second = arrivalWeather.await()
                mutableState.update {
                    it.copy(departure = first, arrival = second, selected = when (it.selected?.airportCode) {
                        first.airportCode -> first
                        second.airportCode -> second
                        else -> it.selected
                    })
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutableState.update { it.copy(routeLoading = false, routeError = error.message ?: "Rota alınamadı. Bağlantınızı kontrol edin.") }
            }
        }
    }

    private suspend fun load(selection: MapSelection): MapSelection = try {
        selection.copy(forecast = weather.forecast(selection.point), loading = false, error = null)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        selection.copy(loading = false, error = "Hava verisi alınamadı. Bağlantınızı kontrol edip yeniden deneyin.")
    }

    fun selectEndpoint(departure: Boolean) {
        selectionJob?.cancel()
        mutableState.update { it.copy(selected = if (departure) it.departure else it.arrival) }
    }

    fun selectPoint(latitude: Double, longitude: Double) {
        if (!validPoint(latitude, longitude)) return
        select(MapSelection(String.format(Locale.ROOT, "%.3f° · %.3f°", latitude, longitude), MapCoordinate(latitude, longitude), loading = true))
    }

    private fun select(selection: MapSelection) {
        selectionJob?.cancel()
        mutableState.update { it.copy(selected = selection.copy(loading = true, error = null)) }
        selectionJob = viewModelScope.launch {
            val result = load(selection)
            mutableState.update { it.copy(selected = result,
                departure = if (result.airportCode != null && it.departure?.airportCode == result.airportCode) result else it.departure,
                arrival = if (result.airportCode != null && it.arrival?.airportCode == result.airportCode) result else it.arrival) }
        }
    }

    fun retrySelection() { state.value.selected?.let(::select) }
    fun closeSelection() {
        selectionJob?.cancel()
        mutableState.update { it.copy(selected = null) }
    }
    fun clearRoute() {
        routeJob?.cancel()
        selectionJob?.cancel()
        mutableState.update { it.copy(route = null, departure = null, arrival = null, selected = null, routeError = null, routeLoading = false) }
    }

    /** Six model sample points per viewport; debounce and ten-minute cache keep pan/zoom economical. */
    fun viewport(points: List<MapCoordinate>) {
        if (points.size !in 2..6 || points.any { !validPoint(it.latitude, it.longitude) }) return
        val key = points.joinToString { "${it.latitude},${it.longitude}" }
        if (key == markerKey) return
        markerKey = key
        markerJob?.cancel()
        markerCache[key]?.takeIf { System.currentTimeMillis() - it.first < 600_000 }?.let { cached ->
            mutableState.update { it.copy(markers = cached.second, markerError = null) }
            return
        }
        mutableState.update { it.copy(markers = emptyList(), markerError = null) }
        markerJob = viewModelScope.launch {
            delay(1_400)
            try {
                val markers = weather.markers(points)
                markerCache[key] = System.currentTimeMillis() to markers
                if (markerCache.size > 20) markerCache.remove(markerCache.keys.first())
                mutableState.update { it.copy(markers = markers, markerError = null) }
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (_: Exception) {
                markerKey = null
                mutableState.update { it.copy(markerError = "Harita hava katmanı alınamadı; bir noktaya dokunarak tekrar deneyin.") }
            }
        }
    }

    private fun AirportNode.selection() = MapSelection("$icao · $name", MapCoordinate(latitude, longitude), icao, loading = true)
    private fun validPoint(lat: Double, lon: Double) = lat.isFinite() && lon.isFinite() && lat in -85.0..85.0 && lon in -180.0..180.0
}
