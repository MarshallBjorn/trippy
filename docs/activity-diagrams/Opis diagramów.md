
# 📖 Dokumentacja Przepływów (Activity Diagrams)

Poniżej znajdują się szczegółowe opisy głównych przypadków użycia (Use Cases) w aplikacji Trippy, wraz z diagramami aktywności (PlantUML) obrazującymi interakcję między aplikacją mobilną a backendem (Spring Boot).

## 1. Rejestracja konta 
**Aktor:** Gość (Aplikacja Mobilna)
**Cel:** Utworzenie nowego konta w systemie, umożliwiającego logowanie.

**Opis przepływu:**
1. Użytkownik wybiera opcję rejestracji i wypełnia formularz (Imię/Nick, E-mail, Hasło, Powtórz hasło).
2. Aplikacja wysyła żądanie do backendu.
3. Backend przeprowadza walidację formatu danych. Jeśli dane są niepoprawne, zwraca błąd `400 Bad Request`.
4. Backend sprawdza w bazie PostgreSQL, czy podany adres e-mail już istnieje. Jeśli tak, zwraca błąd `409 Conflict`.
5. Po udanej weryfikacji serwer haszuje hasło za pomocą algorytmu BCrypt, zapisuje użytkownika w bazie i zwraca status `201 Created`.
6. Aplikacja mobilna wyświetla komunikat o sukcesie i przekierowuje użytkownika do ekranu logowania.

## 2. Logowanie 
**Aktor:** Gość -> Użytkownik Zalogowany
**Cel:** Uwierzytelnienie użytkownika i nadanie mu dostępu do chronionych zasobów (JWT).

**Opis przepływu:**
1. Użytkownik wprowadza swój adres e-mail i hasło.
2. Backend szuka użytkownika w bazie na podstawie adresu e-mail. Brak konta skutkuje błędem `404 Not Found`.
3. System porównuje wprowadzone hasło z haszem z bazy danych (BCrypt). Niezgodność haseł zwraca `401 Unauthorized`.
4. W przypadku sukcesu backend generuje token JWT i zwraca go do klienta ze statusem `200 OK`.
5. Aplikacja mobilna zapisuje token w bezpiecznym magazynie i przechodzi do głównego ekranu (Lista Wycieczek).
## 3. Tworzenie wycieczki 
**Aktor:** Użytkownik Zalogowany
**Cel:** Zainicjowanie nowej podróży w systemie i przypisanie siebie jako jej właściciela.

**Opis przepływu:**
1. Zalogowany użytkownik wypełnia formularz nowej podróży (Nazwa, Data rozpoczęcia, Data zakończenia).
2. Do żądania dołączany jest token JWT.
3. Backend w pierwszej kolejności weryfikuje ważność tokena. Jeśli wygasł lub jest nieprawidłowy, zwraca `401 Unauthorized` (co wymusza wylogowanie w aplikacji).
4. Następuje walidacja danych (np. data zakończenia nie może być wcześniejsza niż rozpoczęcia). Błąd walidacji to `400 Bad Request`.
5. Serwer zapisuje podróż w bazie danych, oznacza twórcę statusem "Owner" i zwraca `201 Created`.
6. Aplikacja odświeża widok, wyświetlając nowo dodaną pozycję.

## 4. Edytowanie wycieczki 
**Aktor:** Użytkownik Zalogowany (tylko Owner)
**Cel:** Aktualizacja podstawowych danych wycieczki (nazwa, ramy czasowe).

**Opis przepływu:**
1. Użytkownik wchodzi w tryb edycji istniejącej wycieczki i zmienia jej dane.
2. Backend sprawdza poprawność tokena JWT (`401 Unauthorized`).
3. Serwer weryfikuje uprawnienia użytkownika. Tylko osoba oznaczona jako "Owner" dla danej podróży może dokonać zmian. W przeciwnym razie system zwraca `403 Forbidden`.
4. Po udanej walidacji formatu danych (`400 Bad Request` w przypadku błędu), rekord w bazie zostaje zaktualizowany, a system zwraca status `200 OK`.
5. Aplikacja wyświetla powiadomienie o sukcesie i aktualizuje szczegóły podróży na ekranie.