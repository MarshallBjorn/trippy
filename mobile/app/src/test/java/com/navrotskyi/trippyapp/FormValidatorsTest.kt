package com.navrotskyi.trippyapp

import com.navrotskyi.trippyapp.ui.viewmodels.FormValidators
import org.junit.Assert.*
import org.junit.Test

class FormValidatorsTest {

    // === EMAIL ===

    @Test fun `prawidlowy email przechodzi`() {
        assertTrue(FormValidators.isValidEmail("jan@example.com"))
        assertTrue(FormValidators.isValidEmail("Jan.Kowalski+test@firma.pl"))
    }

    @Test fun `email bez at nie przechodzi`() {
        assertFalse(FormValidators.isValidEmail("janexample.com"))
    }

    @Test fun `email bez domeny nie przechodzi`() {
        assertFalse(FormValidators.isValidEmail("jan@"))
    }

    @Test fun `pusty email nie przechodzi`() {
        assertFalse(FormValidators.isValidEmail(""))
    }

    @Test fun `email z bialymi znakami jest trymowany`() {
        assertTrue(FormValidators.isValidEmail("  jan@example.com  "))
    }

    // === HASLO ===

    @Test fun `silne haslo przechodzi`() {
        assertTrue(FormValidators.isStrongPassword("Aa1!aaaa"))
        assertTrue(FormValidators.isStrongPassword("MojeHaslo123@"))
    }

    @Test fun `haslo bez wielkiej litery nie przechodzi`() {
        assertFalse(FormValidators.isStrongPassword("aa1!aaaa"))
    }

    @Test fun `haslo bez cyfry nie przechodzi`() {
        assertFalse(FormValidators.isStrongPassword("Aa!aaaaa"))
    }

    @Test fun `haslo bez znaku specjalnego nie przechodzi`() {
        assertFalse(FormValidators.isStrongPassword("Aa1aaaaa"))
    }

    @Test fun `haslo zbyt krotkie nie przechodzi`() {
        assertFalse(FormValidators.isStrongPassword("Aa1!aa"))
    }

    @Test fun `puste haslo nie przechodzi`() {
        assertFalse(FormValidators.isStrongPassword(""))
    }
}