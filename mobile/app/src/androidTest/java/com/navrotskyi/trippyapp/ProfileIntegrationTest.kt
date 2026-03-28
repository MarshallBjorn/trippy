package com.navrotskyi.trippyapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.navrotskyi.trippyapp.data.dao.UserDao
import com.navrotskyi.trippyapp.data.database.UserDatabase
import com.navrotskyi.trippyapp.data.entity.User
import com.navrotskyi.trippyapp.ui.viewmodels.ProfileViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileIntegrationTest {

    private lateinit var userDao: UserDao
    private lateinit var db: UserDatabase
    private lateinit var viewModel: ProfileViewModel

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        db = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        userDao = db.userDao()
        viewModel = ProfileViewModel(userDao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun updateName_shouldUpdateDatabase() = runBlocking {
        val testUser = User(id = 1, name = "Old Name", email = "test@wp.pl", password = "123", currency = "PLN")
        userDao.insertAll(listOf(testUser))

        waitForUserLoading()

        //zmiana imienia
        viewModel.updateUserName("New Name")

        waitForCondition { viewModel.user.value?.name == "New Name" }
        val result = userDao.getAll().first()
        assertEquals("New Name", result[0].name)
    }

    @Test
    fun updateCurrency_shouldUpdateDatabase() = runBlocking {
        userDao.insertAll(listOf(User(id = 1, name = "Jan", email = "j@j.pl", password = "1", currency = "PLN")))
        waitForUserLoading()

        //zmiana waluty
        viewModel.updateCurrency("USD")

        waitForCondition { viewModel.user.value?.currency == "USD" }
        val result = userDao.getAll().first()
        assertEquals("USD", result[0].currency)
    }

    @Test
    fun updatePassword_shouldUpdateDatabase() = runBlocking {
        userDao.insertAll(listOf(User(id = 1, name = "Jan", email = "j@j.pl", password = "old_pass", currency = "PLN")))
        waitForUserLoading()

        //zmiana hasła
        viewModel.updatePassword("new_pass_secure")

        waitForCondition { viewModel.user.value?.password == "new_pass_secure" }
        val result = userDao.getAll().first()
        assertEquals("new_pass_secure", result[0].password)
    }

    @Test
    fun logout_shouldClearUserTable() = runBlocking {
        userDao.insertAll(listOf(User(id = 1, name = "Jan", email = "j@j.pl", password = "1", currency = "PLN")))

        //wylogowanie
        viewModel.logout {  }
        delay(500)


        val result = userDao.getAll().first()
        assertTrue("Database should be empty after logout", result.isEmpty())
    }

    //Funkcje pomocnicze do stabilizacji testów
    private suspend fun waitForUserLoading() {
        var timeout = 0
        while (viewModel.user.value == null && timeout < 20) {
            delay(100)
            timeout++
        }
    }
    private suspend fun waitForCondition(condition: () -> Boolean) {
        var timeout = 0
        while (!condition() && timeout < 20) {
            delay(100)
            timeout++
        }
    }
}