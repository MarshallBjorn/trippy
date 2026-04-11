package com.navrotskyi.trippyapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.navrotskyi.trippyapp.data.dao.UserDao
import com.navrotskyi.trippyapp.data.database.UserDatabase
import com.navrotskyi.trippyapp.data.entity.User
import com.navrotskyi.trippyapp.ui.screens.profile.ProfileScreen
import com.navrotskyi.trippyapp.ui.viewmodels.ProfileViewModel
import com.navrotskyi.trippyapp.api.TrippyApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(AndroidJUnit4::class)
class ProfileIntegrationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockWebServer: MockWebServer
    private lateinit var userDao: UserDao
    private lateinit var db: UserDatabase
    private lateinit var viewModel: ProfileViewModel
    private lateinit var api: TrippyApi

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()

        mockWebServer = MockWebServer()
        mockWebServer.start()

        db = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userDao = db.userDao()

        val baseUrl = mockWebServer.url("/").toString().replace("localhost", "127.0.0.1")

        api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TrippyApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        db.close()
    }
    private fun setMockServerDispatcher(userJson: String, responseCode: Int = 200) {
        mockWebServer.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse {
                if (responseCode != 200) return MockResponse().setResponseCode(responseCode)

                val path = request.path ?: ""
                return if (path.contains("currenc", ignoreCase = true)) {
                    MockResponse().setResponseCode(200).setBody("[]") // Oddaje pustą listę walut
                } else {
                    MockResponse().setResponseCode(200).setBody(userJson) // Oddaje dane usera
                }
            }
        }
    }

    private val dummyJson = """{"id": 1, "name": "Test", "email": "test@test.com", "password": "123", "currency": "PLN", "role": "USER", "isVerified": true, "photoUrl": ""}"""

    @Test
    fun fetchUser_shouldShowDataOnScreen_afterSuccessfulApiCall() {
        val jsonResponse = """
            {
                "id": 1,
                "name": "Mefju Dev",
                "email": "mefju@trippy.pl",
                "password": "123",
                "currency": "EUR",
                "role": "USER",
                "isVerified": true,
                "photoUrl": "http://127.0.0.1/uploads/img.png"
            }
        """.trimIndent()

        setMockServerDispatcher(jsonResponse)
        viewModel = ProfileViewModel(userDao, api)

        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    ProfileScreen(
                        viewModel = viewModel,
                        onEditProfileClick = {},
                        onCurrencyClick = {},
                        onLogoutClick = {},
                        onPasswordClick = {}
                    )
                }
            }
        }

        composeTestRule.waitUntil(5000) {
            try {
                composeTestRule.onNodeWithText("Mefju Dev").assertIsDisplayed()
                true
            } catch (_: AssertionError) { false }
        }

        composeTestRule.onNodeWithText("mefju@trippy.pl").assertIsDisplayed()
    }

    @Test
    fun apiError_shouldShowErrorMessage_whenServerFails() {
        setMockServerDispatcher("", 500)
        viewModel = ProfileViewModel(userDao, api)

        composeTestRule.setContent {
            MaterialTheme {
                Surface {
                    ProfileScreen(viewModel, {}, {}, {}, {})
                }
            }
        }
        composeTestRule.waitUntil(15000) {
            try {
                composeTestRule.onNodeWithText("Nie udało się pobrać danych profilu (Błąd: 500)").assertIsDisplayed()
                true
            } catch (_: AssertionError) { false }
        }
    }
    @Test
    fun updateName_shouldUpdateDatabase() {
        runBlocking {
            val testUser = User(id = 1, name = "Old Name", email = "test@wp.pl", password = "123", currency = "PLN")
            userDao.insertAll(listOf(testUser))

            setMockServerDispatcher(dummyJson)
            viewModel = ProfileViewModel(userDao, api)

            waitForUserLoading()

            viewModel.updateUserName("New Name") { }

            waitForCondition { viewModel.user.value?.name == "New Name" }
            val result = userDao.getAll().first()
            assertEquals("New Name", result[0].name)
        }
    }
    @Test
    fun updateCurrency_shouldUpdateDatabase() {
        runBlocking {
            userDao.insertAll(listOf(User(id = 1, name = "Jan", email = "j@j.pl", password = "1", currency = "PLN")))

            setMockServerDispatcher(dummyJson)
            viewModel = ProfileViewModel(userDao, api)

            waitForUserLoading()

            viewModel.updateCurrency("USD") { }

            waitForCondition { viewModel.user.value?.currency == "USD" }
            val result = userDao.getAll().first()
            assertEquals("USD", result[0].currency)
        }
    }
    @Test
    fun logout_shouldClearUserTable() {
        runBlocking {
            userDao.insertAll(listOf(User(id = 1, name = "Jan", email = "j@j.pl", password = "1", currency = "PLN")))

            setMockServerDispatcher(dummyJson)
            viewModel = ProfileViewModel(userDao, api)

            waitForUserLoading()

            viewModel.logout { }
            delay(500)

            val result = userDao.getAll().first()
            assertTrue("Database should be empty after logout", result.isEmpty())
        }
    }
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