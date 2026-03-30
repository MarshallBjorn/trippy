package com.navrotskyi.trippyapp

import com.navrotskyi.trippyapp.ui.viewmodels.TripViewModel
import org.junit.Assert.*
import org.junit.Test

class TripViewModelTest {


    private val viewModel = TripViewModel()

    @Test
    fun `gdy formularz jest calkowicie pusty, walidacja zwraca false i ustawia bledy`() {

        val isValid = viewModel.validateAddTripForm(
            name = "",
            startDate = "",
            endDate = "",
            budget = ""
        )


        assertFalse(isValid)

        val errors = viewModel.addTripErrors.value
        assertEquals("Nazwa wycieczki jest wymagana", errors.nameError)
        assertEquals("Data rozpoczęcia jest wymagana", errors.startDateError)
        assertEquals("Data zakończenia jest wymagana", errors.endDateError)
        assertEquals("Budżet jest wymagany", errors.budgetError)
    }

    @Test
    fun `gdy data zakonczenia jest przed data rozpoczecia, ustawia blad daty zakonczenia`() {
        // Wykonanie - Celowo dajemy datę powrotu wcześniej niż wyjazdu
        val isValid = viewModel.validateAddTripForm(
            name = "Testowa Wycieczka",
            startDate = "15.05.2026",
            endDate = "10.05.2026",
            budget = "2000"
        )

        // Sprawdzenie
        assertFalse(isValid)
        assertEquals("Data zakończenia musi być po dacie rozpoczęcia", viewModel.addTripErrors.value.endDateError)
    }

    @Test
    fun `gdy budzet jest ujemny, ustawia blad budzetu`() {
        val isValid = viewModel.validateAddTripForm(
            name = "Test",
            startDate = "10.05.2026",
            endDate = "15.05.2026",
            budget = "-500"
        )


        assertFalse(isValid)
        assertEquals("Podaj poprawną kwotę (np. 2000.50)", viewModel.addTripErrors.value.budgetError)
    }

    @Test
    fun `gdy wszystkie dane sa poprawne, walidacja przechodzi i czysci bledy`() {
        val isValid = viewModel.validateAddTripForm(
            name = "Wakacje Życia",
            startDate = "10.05.2026",
            endDate = "24.05.2026",
            budget = "5000.50"
        )

        
        assertTrue(isValid)

        val errors = viewModel.addTripErrors.value
        assertNull(errors.nameError)
        assertNull(errors.startDateError)
        assertNull(errors.endDateError)
        assertNull(errors.budgetError)
    }
}