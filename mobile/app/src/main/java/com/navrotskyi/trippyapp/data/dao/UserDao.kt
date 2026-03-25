package com.navrotskyi.trippyapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.navrotskyi.trippyapp.data.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(users: List<User>)

    @Delete
    suspend fun delete(users: List<User>)

    @Update
    suspend fun update(users: List<User>)

    @Query("SELECT * FROM users")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): User?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE id = :id AND isVerified = 1)")
    suspend fun isVerified(id: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE id = :id AND isBlocked = 1)")
    suspend fun isBlocked(id:Long): Boolean
}