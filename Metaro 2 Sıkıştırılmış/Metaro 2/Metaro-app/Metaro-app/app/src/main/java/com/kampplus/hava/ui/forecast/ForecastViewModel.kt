package com.kampplus.hava.ui.forecast

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.OpenMeteoWeatherRepository
import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class ForecastViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: WeatherRepository
) : ViewModel() {

    constructor(savedStateHandle: SavedStateHandle) : this(savedStateHandle, OpenMeteoWeatherRepository())

    val cityName: String = savedStateHandle.get<String>(ARG_NAME).orEmpty()

    val subtitle: String = savedStateHandle.get<String>(ARG_SUBTITLE).orEmpty()

    private val latitude: Double = savedStateHandle.get<String>(ARG_LATITUDE)?.toDoubleOrNull() ?: 0.0
    private val longitude: Double = savedStateHandle.get<String>(ARG_LONGITUDE)?.toDoubleOrNull() ?: 0.0

    private val _uiState = MutableStateFlow<LoadState<Forecast>>(LoadState.Loading)
    val uiState: StateFlow<LoadState<Forecast>> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = LoadState.Loading
            _uiState.value = try {
                LoadState.Success(repository.getForecast(latitude, longitude))
            } catch (e: IOException) {
                Log.e(TAG, "Tahmin alınamadı: bağlantı hatası", e)
                LoadState.Error("İnternet bağlantını kontrol et.")
            } catch (e: HttpException) {
                Log.e(TAG, "Tahmin alınamadı: HTTP ${e.code()}", e)
                LoadState.Error("Sunucu şu an cevap vermiyor.")
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Tahmin alınamadı: yanıt çözümlenemedi", e)
                LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
            } catch (e: IllegalStateException) {
                // ForecastResponse.toForecast(), "current" bloğu yoksa bunu fırlatır.
                Log.e(TAG, "Tahmin alınamadı: eksik veri", e)
                LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
            }
        }
    }

    companion object {
        private const val TAG = "ForecastViewModel"


        const val ARG_NAME = "name"
        const val ARG_SUBTITLE = "subtitle"
        const val ARG_LATITUDE = "latitude"
        const val ARG_LONGITUDE = "longitude"
    }
}
