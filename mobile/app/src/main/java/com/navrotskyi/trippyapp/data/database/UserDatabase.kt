package com.navrotskyi.trippyapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.navrotskyi.trippyapp.data.dao.UserDao
import com.navrotskyi.trippyapp.data.entity.User


@Database(entities = [User::class], version = 1)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

object UserDb{
    private var db: UserDatabase? = null

    fun getInstance(context: Context): UserDatabase {
        if (db == null){
            db = Room.databaseBuilder(
                context,
                UserDatabase::class.java,
                "users.db").build()
        }
        return db!!
    }
}
