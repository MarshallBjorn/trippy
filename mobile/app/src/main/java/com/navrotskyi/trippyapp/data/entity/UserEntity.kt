package com.navrotskyi.trippyapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index
import androidx.room.ColumnInfo

@Entity(
    tableName = "users",
    indices = [
        Index(
            value = ["email"],
            unique = true
        )
    ]
)
data class User(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "name")
    val name: String? = null,

    @ColumnInfo(name = "password")
    val password: String? = null,

    @ColumnInfo(name = "role")
    val role: String? = null
)