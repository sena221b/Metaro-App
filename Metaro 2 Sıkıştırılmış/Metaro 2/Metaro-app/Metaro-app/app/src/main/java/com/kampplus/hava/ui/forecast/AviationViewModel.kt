package com.kampplus.hava.ui.forecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.AirportRepository
import com.kampplus.hava.data.AviationApi
import com.kampplus.hava.data.AviationApiClient
import com.kampplus.hava.model.AirportNode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import java.time.Instant

data class AviationUiState(
    val loading: Boolean = false,
    val airport: AirportNode? = null,
    val metar: String = "",
    val taf: String = "",
    val metarError: String? = null,
    val tafError: String? = null,
    val error: String? = null,
    val fetchedAt: Instant? = null
)

class AviationViewModel(
    private val api: AviationApi = AviationApiClient.instance,
    private val airports: AirportRepository = AirportRepository(api)
) : ViewModel() {
    private val state = MutableStateFlow(AviationUiState())
    val uiState = state.asStateFlow()
    private var job: Job? = null

    fun search(code: String) {
        job?.cancel()
        state.value = AviationUiState(loading = true)
        job = viewModelScope.launch {
            try {
                val airport = airports.resolve(code)
                val metar = async { fetch { api.getMetar(airport.icao) } }
                val taf = async { fetch { api.getTaf(airport.icao) } }
                val m = metar.await()
                val t = taf.await()
                state.value = AviationUiState(airport = airport, metar = m.first, taf = t.first,
                    metarError = m.second, tafError = t.second, fetchedAt = Instant.now())
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) {
                state.value = AviationUiState(error = error.message ?: "Bağlantı kurulamadı. Yeniden deneyin.")
            }
        }
    }

    private suspend fun fetch(call: suspend () -> Response<String>): Pair<String, String?> = try {
        val response = call()
        if (!response.isSuccessful) "" to "Servis yanıtı: HTTP ${response.code()}. Yeniden deneyin."
        else response.body().orEmpty().trim() to null
    } catch (cancelled: CancellationException) { throw cancelled
    } catch (error: Exception) { "" to "Veri alınamadı. Bağlantınızı kontrol edip yeniden deneyin." }
}
