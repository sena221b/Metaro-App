package com.kampplus.hava.data

import com.kampplus.hava.model.City
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface CityRepository {
    /** İsme göre şehir arar. Sonuç yoksa boş liste döner. */
    suspend fun search(query: String): List<City>
}

class OpenMeteoCityRepository(
    private val api: CityApi = ApiClient.cityApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CityRepository {

    override suspend fun search(query: String): List<City> = withContext(ioDispatcher) {

        val response = api.search(query.trim())
        response.results.orEmpty()
    }
}
