package com.navrotskyi.trippyapp.data.repository

import com.navrotskyi.trippyapp.api.TrippyApi
import com.navrotskyi.trippyapp.data.dao.TripDao
import com.navrotskyi.trippyapp.data.dao.TripNodeDao
import com.navrotskyi.trippyapp.data.mapper.toDomain
import com.navrotskyi.trippyapp.data.mapper.toEntity
import com.navrotskyi.trippyapp.models.Trip
import com.navrotskyi.trippyapp.models.TripNodeDto
import com.navrotskyi.trippyapp.data.mapper.toDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TripRepository(
    private val api: TrippyApi,
    private val tripDao: TripDao,
    private val tripNodeDao: TripNodeDao
) {

    fun observeTrips(): Flow<List<Trip>> =
        tripDao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeTrip(tripId: String): Flow<Trip?> =
        tripDao.observeById(tripId).map { it?.toDomain() }

    fun observeNodes(tripId: String): Flow<List<TripNodeDto>> =
        tripNodeDao.observeByTrip(tripId).map { list -> list.map { it.toDto() } }

    fun observeNode(nodeId: String): Flow<TripNodeDto?> =
        tripNodeDao.observeById(nodeId).map { it?.toDto() }

    suspend fun refreshTrips(): Result<Unit> = runCatching {
        val response = api.getMyTrips()
        if (response.isSuccessful && response.body() != null) {
            val entities = response.body()!!.map { it.toEntity() }
            tripDao.replaceAll(entities)
        } else {
            throw IllegalStateException("HTTP ${response.code()}")
        }
    }

    suspend fun refreshNodes(tripId: String): Result<Unit> = runCatching {
        val response = api.getTripNodes(tripId)
        if (response.isSuccessful && response.body() != null) {
            val entities = response.body()!!.map { it.toEntity(tripId) }
            tripNodeDao.replaceForTrip(tripId, entities)
        } else {
            throw IllegalStateException("HTTP ${response.code()}")
        }
    }

    suspend fun refreshNode(tripId: String, nodeId: String): Result<Unit> = runCatching {
        val response = api.getTripNode(tripId, nodeId)
        if (response.isSuccessful && response.body() != null) {
            tripNodeDao.upsert(response.body()!!.toEntity(tripId))
        } else {
            throw IllegalStateException("HTTP ${response.code()}")
        }
    }

    suspend fun clearCache() {
        tripDao.deleteAll()
    }
}