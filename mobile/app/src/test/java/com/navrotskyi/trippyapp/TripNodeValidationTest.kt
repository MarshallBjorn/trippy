package com.navrotskyi.trippyapp

import com.navrotskyi.trippyapp.ui.viewmodels.TripViewModel
import org.junit.Assert.*
import org.junit.Test

class TripNodeValidationTest {
    private val vm = TripViewModel()

    @Test
    fun `pusty formularz wezla zwraca bledy`() {
        assertFalse(vm.validateTripNodeForm("", "", "", ""))
        val e = vm.nodeFormErrors.value
        assertNotNull(e.nameError)
        assertNotNull(e.startTimeError)
        assertNotNull(e.endTimeError)
        assertNull(e.priceError) // cena pusta jest ok
    }

    @Test
    fun `niepoprawny format daty`() {
        assertFalse(vm.validateTripNodeForm("Lot", "10/05/2026 12:00", "10.05.2026 14:00", ""))
        assertNotNull(vm.nodeFormErrors.value.startTimeError)
    }

    @Test
    fun `koniec przed startem`() {
        assertFalse(vm.validateTripNodeForm("Lot", "10.05.2026 14:00", "10.05.2026 12:00", ""))
        assertEquals("Koniec musi być po rozpoczęciu", vm.nodeFormErrors.value.endTimeError)
    }

    @Test
    fun `ujemna cena - ustawia blad`() {
        assertFalse(vm.validateTripNodeForm("Lot", "10.05.2026 12:00", "10.05.2026 14:00", "-50"))
        assertNotNull(vm.nodeFormErrors.value.priceError)
    }

    @Test
    fun `poprawne dane przechodza walidacje`() {
        assertTrue(vm.validateTripNodeForm("Lot", "10.05.2026 12:00", "10.05.2026 14:00", "150,50"))
    }

    @Test
    fun `walidacja zaproszenia - poprawny email`() {
        assertTrue(vm.validateInviteForm("test@example.com"))
        assertNull(vm.inviteFormErrors.value.emailError)
    }

    @Test
    fun `walidacja zaproszenia - niepoprawny email`() {
        assertFalse(vm.validateInviteForm("blah"))
        assertEquals("Niepoprawny format e-mail", vm.inviteFormErrors.value.emailError)
    }
}