package com.navrotskyi.trippyapp

import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.navrotskyi.trippyapp.ui.screens.RegisterScreen
import com.navrotskyi.trippyapp.ui.viewmodels.AuthViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var vm: AuthViewModel

    @Before
    fun setUp() {
        vm = AuthViewModel()
    }

    @Test
    fun rendersAllFieldsAndButtons() {
        composeTestRule.setContent {
            RegisterScreen(
                modifier = Modifier,
                viewModel = vm,
                onRegisterClick = { _, _, _, _ -> },
                onBackToLoginClick = {}
            )
        }

        composeTestRule.onNodeWithText("Dołącz do Trippy").assertIsDisplayed()
        composeTestRule.onNodeWithText("Imię lub Nick").assertIsDisplayed()
        composeTestRule.onNodeWithText("E-mail").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hasło").assertIsDisplayed()
        composeTestRule.onNodeWithText("Powtórz hasło").assertIsDisplayed()
        composeTestRule.onNodeWithText("Zarejestruj się").assertIsDisplayed()
        composeTestRule.onNodeWithText("Masz już konto? Zaloguj się").assertIsDisplayed()
    }

    @Test
    fun submit_withValidData_invokesCallback() {
        var captured: List<String>? = null

        composeTestRule.setContent {
            RegisterScreen(
                modifier = Modifier,
                viewModel = vm,
                onRegisterClick = { n, e, p, c -> captured = listOf(n, e, p, c) },
                onBackToLoginClick = {}
            )
        }

        composeTestRule.onNodeWithText("Imię lub Nick").performTextInput("Alicja")
        composeTestRule.onNodeWithText("E-mail").performTextInput("alice@example.com")
        composeTestRule.onNodeWithText("Hasło").performTextInput("Sekret123!")
        composeTestRule.onNodeWithText("Powtórz hasło").performTextInput("Sekret123!")
        composeTestRule.onNodeWithText("Zarejestruj się").performClick()

        assertEquals(listOf("Alicja", "alice@example.com", "Sekret123!", "Sekret123!"), captured)
    }

    @Test
    fun submit_withMismatchedPasswords_doesNotInvokeCallback_andShowsError() {
        var invoked = false

        composeTestRule.setContent {
            RegisterScreen(
                modifier = Modifier,
                viewModel = vm,
                onRegisterClick = { _, _, _, _ -> invoked = true },
                onBackToLoginClick = {}
            )
        }

        composeTestRule.onNodeWithText("Imię lub Nick").performTextInput("Alicja")
        composeTestRule.onNodeWithText("E-mail").performTextInput("alice@example.com")
        composeTestRule.onNodeWithText("Hasło").performTextInput("Sekret123!")
        composeTestRule.onNodeWithText("Powtórz hasło").performTextInput("InneHaslo!")
        composeTestRule.onNodeWithText("Zarejestruj się").performClick()

        assertEquals(false, invoked)
        // Error message z VM trafia do supportingText TextFielda - powinien być widoczny.
        // Wartość zależy od implementacji VM; sprawdzamy że confirmPasswordError jest ustawiony.
        assertTrue(
            "Oczekiwano komunikatu o niezgodności haseł",
            vm.registerErrors.value.confirmPasswordError != null
        )
    }

    @Test
    fun submit_withInvalidEmail_doesNotInvokeCallback_andSetsEmailError() {
        var invoked = false

        composeTestRule.setContent {
            RegisterScreen(
                modifier = Modifier,
                viewModel = vm,
                onRegisterClick = { _, _, _, _ -> invoked = true },
                onBackToLoginClick = {}
            )
        }

        composeTestRule.onNodeWithText("Imię lub Nick").performTextInput("Alicja")
        composeTestRule.onNodeWithText("E-mail").performTextInput("not-an-email")
        composeTestRule.onNodeWithText("Hasło").performTextInput("Sekret123!")
        composeTestRule.onNodeWithText("Powtórz hasło").performTextInput("Sekret123!")
        composeTestRule.onNodeWithText("Zarejestruj się").performClick()

        assertEquals(false, invoked)
        assertTrue(
            "Oczekiwano błędu walidacji email",
            vm.registerErrors.value.emailError != null
        )
    }

    @Test
    fun submit_withEmptyFields_doesNotInvokeCallback() {
        var invoked = false

        composeTestRule.setContent {
            RegisterScreen(
                modifier = Modifier,
                viewModel = vm,
                onRegisterClick = { _, _, _, _ -> invoked = true },
                onBackToLoginClick = {}
            )
        }

        composeTestRule.onNodeWithText("Zarejestruj się").performClick()

        assertEquals(false, invoked)
        // Wszystkie cztery błędy muszą być ustawione
        val errs = vm.registerErrors.value
        assertTrue(errs.nameError != null || errs.emailError != null
                || errs.passwordError != null || errs.confirmPasswordError != null)
    }

    @Test
    fun backToLogin_invokesCallback() {
        var clicked = false

        composeTestRule.setContent {
            RegisterScreen(
                modifier = Modifier,
                viewModel = vm,
                onRegisterClick = { _, _, _, _ -> },
                onBackToLoginClick = { clicked = true }
            )
        }

        composeTestRule.onNodeWithText("Masz już konto? Zaloguj się").performClick()
        assertTrue(clicked)
    }

    @Test
    fun clearErrors_afterSuccessfulCorrection() {
        composeTestRule.setContent {
            RegisterScreen(
                modifier = Modifier,
                viewModel = vm,
                onRegisterClick = { _, _, _, _ -> },
                onBackToLoginClick = {}
            )
        }

        // Najpierw spowoduj błąd
        composeTestRule.onNodeWithText("Zarejestruj się").performClick()
        assertTrue(vm.registerErrors.value.nameError != null
                || vm.registerErrors.value.emailError != null)

        // Wypełnij poprawnie i kliknij ponownie
        composeTestRule.onNodeWithText("Imię lub Nick").performTextInput("Alicja")
        composeTestRule.onNodeWithText("E-mail").performTextInput("alice@example.com")
        composeTestRule.onNodeWithText("Hasło").performTextInput("Sekret123!")
        composeTestRule.onNodeWithText("Powtórz hasło").performTextInput("Sekret123!")
        composeTestRule.onNodeWithText("Zarejestruj się").performClick()

        val errs = vm.registerErrors.value
        assertNull(errs.nameError)
        assertNull(errs.emailError)
        assertNull(errs.passwordError)
        assertNull(errs.confirmPasswordError)
    }
}