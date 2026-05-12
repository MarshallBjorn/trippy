package com.navrotskyi.trippyapp

import com.navrotskyi.trippyapp.ui.viewmodels.AuthViewModel
import org.junit.Assert.*
import org.junit.Test

class AuthViewModelValidationTest {

    private val vm = AuthViewModel()

    @Test
    fun `pusty formularz - wszystkie bledy ustawione`() {
        assertFalse(vm.validateRegisterForm("", "", "", ""))
        val e = vm.registerErrors.value
        assertNotNull(e.nameError)
        assertNotNull(e.emailError)
        assertNotNull(e.passwordError)
        assertNotNull(e.confirmPasswordError)
    }

    @Test
    fun `niepoprawny email - ustawia blad emaila`() {
        assertFalse(vm.validateRegisterForm("Jan", "nie-email", "Aa1!aaaa", "Aa1!aaaa"))
        assertEquals("Niepoprawny format e-mail", vm.registerErrors.value.emailError)
    }

    @Test
    fun `slabe haslo - ustawia blad hasla`() {
        assertFalse(vm.validateRegisterForm("Jan", "a@b.pl", "abc", "abc"))
        assertNotNull(vm.registerErrors.value.passwordError)
    }

    @Test
    fun `rozne hasla - blad confirm`() {
        assertFalse(vm.validateRegisterForm("Jan", "a@b.pl", "Aa1!aaaa", "Aa1!aaab"))
        assertEquals("Hasła nie są identyczne", vm.registerErrors.value.confirmPasswordError)
    }

    @Test
    fun `poprawne dane - waliduje sie i czysci bledy`() {
        assertTrue(vm.validateRegisterForm("Jan", "jan@example.com", "Aa1!aaaa", "Aa1!aaaa"))
        val e = vm.registerErrors.value
        assertNull(e.nameError); assertNull(e.emailError)
        assertNull(e.passwordError); assertNull(e.confirmPasswordError)
    }
}