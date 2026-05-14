package com.navrotskyi.trippyapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.navrotskyi.trippyapp.data.dao.TripDao
import com.navrotskyi.trippyapp.data.dao.TripNodeDao
import com.navrotskyi.trippyapp.data.dao.UserDao
import com.navrotskyi.trippyapp.data.entity.TripEntity
import com.navrotskyi.trippyapp.data.entity.TripNodeEntity
import com.navrotskyi.trippyapp.data.entity.User

@Database(
    entities = [
        User::class,
        TripEntity::class,
        TripNodeEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class TrippyDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun tripDao(): TripDao
    abstract fun tripNodeDao(): TripNodeDao
}

object TrippyDb {
    @Volatile
    private var db: TrippyDatabase? = null

    fun getInstance(context: Context): TrippyDatabase {
        return db ?: synchronized(this) {
            db ?: Room.databaseBuilder(
                context.applicationContext,
                TrippyDatabase::class.java,
                "trippy.db"
            )
                .fallbackToDestructiveMigration()
                .build()
                .also { db = it }
        }
    }
}