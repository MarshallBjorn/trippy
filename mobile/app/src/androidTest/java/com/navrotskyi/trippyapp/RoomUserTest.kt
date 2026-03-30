package com.navrotskyi.trippyapp

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.navrotskyi.trippyapp.data.dao.UserDao
import com.navrotskyi.trippyapp.data.database.UserDatabase
import com.navrotskyi.trippyapp.data.entity.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserDaoTest {

    private lateinit var userDao: UserDao
    private lateinit var db: UserDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAll_and_getAll_returnsInsertedUsers() = runTest {
        // given
        val users = listOf(
            User(name = "Jan", email = "jan@example.com"),
            User(name = "Anna", email = "anna@example.com")
        )

        // when
        userDao.insertAll(users)

        // then
        val result = userDao.getAll().first()
        assertEquals(2, result.size)
        assertEquals("Jan", result[0].name)
        assertEquals("anna@example.com", result[1].email)
        assertTrue(result[0].id > 0)           // sprawdzamy, że id zostało nadane
        assertTrue(result[1].id > 0)
    }

    @Test
    fun delete_removesUsers() = runTest {
        // given
        val userToInsert = User(name = "Tomasz", email = "tomasz@test.pl")
        userDao.insertAll(listOf(userToInsert))

        val insertedUsers = userDao.getAll().first()
        assertEquals(1, insertedUsers.size) // sanity check

        val insertedUser = insertedUsers[0]

        // when
        userDao.delete(listOf(insertedUser))

        // then
        val result = userDao.getAll().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun insert_duplicateId_replacesWhenOnConflictReplace() = runTest {
        val user1 = User(name = "Stary", email = "konflikt@example.com")
        userDao.insertAll(listOf(user1))

        val user2 = User(name = "Nowy", email = "konflikt@example.com")

        // when
        userDao.insertAll(listOf(user2))

        // then
        val result = userDao.getAll().first()
        assertEquals(1, result.size)
        assertEquals("Nowy", result[0].name)
        assertEquals("konflikt@example.com", result[0].email)
    }
}