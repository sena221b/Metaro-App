package com.kampplus.hava.ui.city

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.data.OpenMeteoCityRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class CityViewModel(
    private val repository: CityRepository = OpenMeteoCityRepository()
) : ViewModel() {

    private val _query = MutableStateFlow(INITIAL_QUERY)
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<LoadState<List<City>>>(LoadState.Loading)
    val uiState: StateFlow<LoadState<List<City>>> = _uiState.asStateFlow()

    init {
        search()
    }
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun search() {
        viewModelScope.launch {
            _uiState.value = LoadState.Loading
            _uiState.value = try {
                val cities = repository.search(_query.value)
                // Cevap geldi ama içi boş: bu bir hata değil, "Empty" durumudur.
                if (cities.isEmpty()) LoadState.Empty else LoadState.Success(cities)
            } catch (e: IOException) {
                // İnternet yok, DNS çözülemedi veya zaman aşımı.
                Log.e(TAG, "Arama başarısız: bağlantı hatası", e)
                LoadState.Error("İnternet bağlantınızı kontrol edin.")
            } catch (e: HttpException) {
                // Sunucu yanıt verdi ama 4xx/5xx döndü.
                Log.e(TAG, "Arama başarısız: HTTP ${e.code()}", e)
                LoadState.Error("Sunucu şu an cevap vermiyor.")
            } catch (e: JsonSyntaxException) {
                // Beklenmeyen JSON: model ile yanıt uyuşmuyor.
                Log.e(TAG, "Arama başarısız: yanıt çözümlenemedi", e)
                LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
            }
            // Kullanıcıya sade mesaj, Logcat'e teknik ayrıntı verecektir.
        }
    }

    private companion object {
        const val TAG = "CityViewModel"
        const val INITIAL_QUERY = "İstanbul"
    }
}
