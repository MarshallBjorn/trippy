package com.navrotskyi.trippyapp.api

/**
 * Tłumaczenie komunikatów błędów z backendu (po angielsku) na spójne, polskie komunikaty.
 *
 * Backend zwraca komunikaty po angielsku (np. "User not found with email ..."),
 * a reszta aplikacji jest po polsku. Ten obiekt mapuje znane przypadki na polskie
 * teksty, dzięki czemu użytkownik nie widzi mieszanki języków.
 */
object ErrorMessages {

    private const val GENERIC = "Coś poszło nie tak. Spróbuj ponownie."

    /** Tłumaczy pojedynczy komunikat tekstowy z serwera na polski. */
    fun translate(raw: String?): String {
        if (raw.isNullOrBlank()) return GENERIC
        val lower = raw.lowercase()
        return when {
            // Rejestracja zajętym adresem e-mail
            lower.contains("email") && (lower.contains("already") || lower.contains("taken") || lower.contains("in use") || lower.contains("exists")) ->
                "Ten adres e-mail jest już zajęty."

            // Zaproszenie nieistniejącego użytkownika
            lower.contains("user not found") || (lower.contains("not found") && lower.contains("email")) ->
                "Nie znaleziono użytkownika o podanym adresie e-mail."

            // Ponowne zaproszenie tej samej osoby
            (lower.contains("already") && (lower.contains("invite") || lower.contains("participant") || lower.contains("member"))) ->
                "Ten użytkownik został już zaproszony do tej podróży."

            // Błędne dane logowania
            lower.contains("invalid") && (lower.contains("credential") || lower.contains("password") || lower.contains("login")) ->
                "Nieprawidłowy e-mail lub hasło."

            lower.contains("not verified") || lower.contains("not confirmed") ->
                "Konto nie zostało jeszcze zweryfikowane. Sprawdź swoją skrzynkę e-mail."

            // Jeśli komunikat jest już po polsku (zawiera polskie znaki / typowe słowa), zostaw go.
            looksPolish(raw) -> raw

            else -> raw
        }
    }

    /**
     * Tłumaczy wyjątek na polski komunikat. Rozróżnia brak połączenia od błędu biznesowego z API,
     * dzięki czemu błąd "User not found" nie jest już prezentowany jako "Brak połączenia".
     */
    fun fromThrowable(e: Throwable): String = when (e) {
        is NoInternetException -> e.message ?: GENERIC
        is ApiException -> translate(e.message)
        else -> translate(e.localizedMessage ?: e.message)
    }

    private fun looksPolish(text: String): Boolean {
        val polishChars = "ąćęłńóśźżĄĆĘŁŃÓŚŹŻ"
        if (text.any { it in polishChars }) return true
        val lower = text.lowercase()
        return listOf("błąd", "brak", "nie ", "hasło", "adres", "podróż", "wycieczk")
            .any { lower.contains(it) }
    }
}
