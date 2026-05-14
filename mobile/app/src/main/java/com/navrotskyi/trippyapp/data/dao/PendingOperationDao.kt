package com.navrotskyi.trippyapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.navrotskyi.trippyapp.data.entity.PendingOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {

    @Insert
    suspend fun insert(op: PendingOperationEntity): Long

    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC")
    suspend fun getAll(): List<PendingOperationEntity>

    @Query("SELECT COUNT(*) FROM pending_operations")
    fun observeCount(): Flow<Int>

    @Delete
    suspend fun delete(op: PendingOperationEntity)

    @Update
    suspend fun update(op: PendingOperationEntity)

    @Query("DELETE FROM pending_operations")
    suspend fun deleteAll()
}