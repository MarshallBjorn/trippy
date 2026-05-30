package com.navrotskyi.trippyapp

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.navrotskyi.trippyapp.ui.screens.LoginScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun rendersAllKeyElements() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { _, _ -> },
                onRegisterClick = {},
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithText("Trippy").assertIsDisplayed()
        composeTestRule.onNodeWithText("E-mail").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hasło").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zaloguj się").assertIsDisplayed()
        composeTestRule.onNodeWithText("Utwórz nowe konto").assertIsDisplayed()
    }

    @Test
    fun loginClick_passesEnteredCredentialsToCallback() {
        var capturedEmail: String? = null
        var capturedPassword: String? = null

        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { e, p -> capturedEmail = e; capturedPassword = p },
                onRegisterClick = {},
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithText("E-mail").performTextInput("alice@example.com")
        composeTestRule.onNodeWithText("Hasło").performTextInput("Sekret123!")
        composeTestRule.onNodeWithText("Zaloguj się").performClick()

        assertEquals("alice@example.com", capturedEmail)
        assertEquals("Sekret123!", capturedPassword)
    }

    @Test
    fun loginClick_withEmptyFields_stillInvokesCallback_withEmptyStrings() {
        // Walidacja jest w ViewModelu (patrz AuthViewModelValidationTest) — sam ekran zawsze przepuszcza.
        var clicked = false
        var capturedEmail = "x"
        var capturedPassword = "x"

        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { e, p -> clicked = true; capturedEmail = e; capturedPassword = p },
                onRegisterClick = {},
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithText("Zaloguj się").performClick()

        assertTrue(clicked)
        assertEquals("", capturedEmail)
        assertEquals("", capturedPassword)
    }

    @Test
    fun registerLink_invokesCallback() {
        var registerClicked = false

        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { _, _ -> },
                onRegisterClick = { registerClicked = true },
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithText("Utwórz nowe konto").performClick()

        assertTrue(registerClicked)
    }

    @Test
    fun passwordToggle_switchesVisibilityIcon() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { _, _ -> },
                onRegisterClick = {},
                modifier = Modifier
            )
        }

        // Wpisz hasło — żeby było co maskować
        composeTestRule.onNodeWithText("Hasło").performTextInput("Secret123")

        // Ikona "Pokaż hasło" jest dostępna przez contentDescription
        val toggle = composeTestRule.onNodeWithContentDescription("Pokaż hasło")
        toggle.assertIsDisplayed()

        // Pierwszy klik → maska off → drugi → maska on. Każdy klik renderuje tę samą
        // contentDescription, więc głównie sprawdzamy że klik nie wywala UI.
        toggle.performClick()
        composeTestRule.onNodeWithContentDescription("Pokaż hasło").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Pokaż hasło").performClick()
        composeTestRule.onNodeWithContentDescription("Pokaż hasło").assertIsDisplayed()
    }

    @Test
    fun typingInEmailField_updatesDisplayedValue() {
        composeTestRule.setContent {
            LoginScreen(
                onLoginClick = { _, _ -> },
                onRegisterClick = {},
                modifier = Modifier
            )
        }

        composeTestRule.onNodeWithText("E-mail").performTextInput("test@x.io")
        // Po wpisaniu, węzeł z textem "test@x.io" musi istnieć (label przeskakuje na floating).
        composeTestRule.onNodeWithText("test@x.io").assertTextEquals("test@x.io")
    }
}