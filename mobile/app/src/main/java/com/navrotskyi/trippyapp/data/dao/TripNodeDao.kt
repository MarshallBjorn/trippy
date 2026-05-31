package com.navrotskyi.trippyapp.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.navrotskyi.trippyapp.data.entity.TripNodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TripNodeDao {

    @Query("SELECT * FROM trip_nodes WHERE tripId = :tripId ORDER BY startTime ASC")
    fun observeByTrip(tripId: String): Flow<List<TripNodeEntity>>

    @Query("SELECT * FROM trip_nodes WHERE id = :nodeId LIMIT 1")
    fun observeById(nodeId: String): Flow<TripNodeEntity?>

    @Query("SELECT * FROM trip_nodes WHERE id = :nodeId LIMIT 1")
    suspend fun getById(nodeId: String): TripNodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(nodes: List<TripNodeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(node: TripNodeEntity)

    @Query("DELETE FROM trip_nodes WHERE id = :nodeId")
    suspend fun deleteById(nodeId: String)

    @Query("DELETE FROM trip_nodes WHERE tripId = :tripId")
    suspend fun deleteByTrip(tripId: String)

    @Transaction
    suspend fun replaceForTrip(tripId: String, nodes: List<TripNodeEntity>) {
        deleteByTrip(tripId)
        upsertAll(nodes)
    }
}