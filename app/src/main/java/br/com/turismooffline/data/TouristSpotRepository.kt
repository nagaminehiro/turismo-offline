package br.com.turismooffline.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TouristSpotRepository(private val database: TouristSpotDbHelper) {
    suspend fun getAll(): List<TouristSpot> = withContext(Dispatchers.IO) { database.findAll() }

    suspend fun save(spot: TouristSpot): Long = withContext(Dispatchers.IO) { database.save(spot) }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) { database.delete(id) }
}
