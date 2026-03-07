
# Dokumentacja Przepływów (Activity Diagrams)

## 1. Rejestracja konta 
**Aktor:** Gość (Aplikacja Mobilna)  
**Cel:** Utworzenie nowego konta w systemie, umożliwiającego logowanie.

### Warunki wejściowe (Preconditions)
* Użytkownik pobrał i uruchomił aplikację.
* Użytkownik nie jest obecnie zalogowany.
* Urządzenie posiada aktywne połączenie z internetem.

### Opis przepływu głównego
1. Użytkownik wybiera opcję rejestracji i wypełnia formularz (Imię/Nick, E-mail, Hasło, Powtórz hasło).
2. Aplikacja wysyła żądanie do backendu.
3. Backend przeprowadza walidację formatu danych.
4. Backend sprawdza w bazie PostgreSQL, czy podany adres e-mail już istnieje.
5. Po udanej weryfikacji serwer haszuje hasło za pomocą algorytmu BCrypt, zapisuje użytkownika w bazie i zwraca status `201 Created`.
6. Aplikacja mobilna wyświetla komunikat o sukcesie i przekierowuje użytkownika do ekranu logowania.

### Scenariusze alternatywne
* **Błąd walidacji (`400 Bad Request`):** Hasła się nie zgadzają, format e-maila jest niepoprawny lub hasło jest za krótkie. Aplikacja prosi o poprawę danych.
* **E-mail zajęty (`409 Conflict`):** Backend wykrywa, że konto z tym adresem już istnieje.
* **Brak połączenia/Timeout:** Aplikacja informuje o problemie z siecią i pozwala ponowić próbę.
* **Anulowanie akcji:** Użytkownik w dowolnym momencie przed wysłaniem formularza klika "Powrót do logowania". Proces zostaje przerwany.

### Warunki wyjściowe (Postconditions)
* Nowy użytkownik został trwale zapisany w bazie danych PostgreSQL.
* Hasło użytkownika jest bezpiecznie zahaszowane.
* Użytkownik może teraz użyć swoich danych do zalogowania się w systemie.



## 2. Logowanie
**Aktor:** Gość -> Użytkownik Zalogowany  
**Cel:** Uwierzytelnienie użytkownika i nadanie mu dostępu do chronionych zasobów (JWT).

### Warunki wejściowe (Preconditions)
* Użytkownik posiada zarejestrowane i aktywne konto w systemie.
* Użytkownik znajduje się na ekranie logowania i nie jest uwierzytelniony.

### Opis przepływu głównego
1. Użytkownik wprowadza swój adres e-mail i hasło.
2. Backend szuka użytkownika w bazie na podstawie adresu e-mail.
3. System porównuje wprowadzone hasło z haszem z bazy danych (BCrypt).
4. W przypadku sukcesu backend generuje token JWT i zwraca go do klienta ze statusem `200 OK`.
5. Aplikacja mobilna zapisuje token w bezpiecznym magazynie i przechodzi do ekranu "Lista Wycieczek".

### Scenariusze alternatywne
* **Brak konta (`404 Not Found`):** Użytkownik z podanym adresem e-mail nie istnieje w bazie.
* **Błędne hasło (`401 Unauthorized`):** Hasła nie pasują do siebie. System odrzuca żądanie ze względów bezpieczeństwa bez podawania szczegółów (komunikat ogólny: "Błędny e-mail lub hasło").
* **Niedostępność bazy danych (`500 Internal Server Error`):** Błąd po stronie infrastruktury backendowej.

### Warunki wyjściowe (Postconditions)
* Aplikacja mobilna bezpiecznie przechowuje ważny token JWT.
* Stan aplikacji zmienia się na "Zalogowany", co odblokowuje dostęp do głównych funkcji systemu (np. przeglądania i tworzenia wycieczek).



## 3. Tworzenie wycieczki 
**Aktor:** Użytkownik Zalogowany  
**Cel:** Zainicjowanie nowej podróży w systemie i przypisanie siebie jako jej właściciela.

### Warunki wejściowe (Preconditions)
* Użytkownik jest pomyślnie zalogowany w aplikacji.
* Aplikacja dysponuje ważnym (niewygasłym) tokenem JWT.

### Opis przepływu głównego
1. Zalogowany użytkownik wypełnia formularz nowej podróży (Nazwa, Data rozpoczęcia, Data zakończenia).
2. Do żądania dołączany jest token JWT.
3. Backend weryfikuje ważność tokena.
4. Następuje walidacja danych (np. data zakończenia > rozpoczęcia).
5. Serwer zapisuje podróż w bazie danych, oznacza twórcę jako "Owner" i zwraca `201 Created`.
6. Aplikacja odświeża widok, wyświetlając nowo dodaną pozycję na liście.

### Scenariusze alternatywne
* **Niewymagane pola (opcjonalne):** Użytkownik podaje tylko nazwę podróży, omijając ramy czasowe (o ile MVP zakłada je jako opcjonalne).
* **Token wygasł (`401 Unauthorized`):** Czas życia tokena JWT minął w trakcie wypełniania formularza. Aplikacja usuwa stary token i przekierowuje użytkownika na ekran logowania.
* **Błędne daty (`400 Bad Request`):** Użytkownik podał datę powrotu wcześniejszą niż data wyjazdu.

### Warunki wyjściowe (Postconditions)
* Nowy rekord wycieczki istnieje w bazie danych.
* Aktor automatycznie staje się Właścicielem (Owner) utworzonej wycieczki i jest do niej przypisany jako uczestnik.
* Bilans nowej wycieczki dla wszystkich (obecnie jednego) uczestników wynosi domyślnie 0.



## 4. Edytowanie wycieczki 
**Aktor:** Użytkownik Zalogowany (tylko Owner)  
**Cel:** Aktualizacja podstawowych danych wycieczki (nazwa, ramy czasowe).

### Warunki wejściowe (Preconditions)
* Użytkownik jest pomyślnie zalogowany i posiada ważny token JWT.
* Wybrana wycieczka istnieje w systemie.
* Użytkownik ma przypisaną rolę "Owner" (Właściciel) dla tej konkretnej wycieczki.

### Opis przepływu głównego
1. Użytkownik wchodzi w tryb edycji istniejącej wycieczki i zmienia jej dane (Nazwa lub Daty).
2. Backend sprawdza poprawność tokena JWT.
3. Serwer weryfikuje uprawnienia użytkownika (czy jest "Ownerem").
4. Po udanej walidacji formatu danych, rekord w bazie zostaje zaktualizowany, a system zwraca status `200 OK`.
5. Aplikacja wyświetla powiadomienie o sukcesie i aktualizuje szczegóły podróży na ekranie.

### Scenariusze alternatywne
* **Brak uprawnień (`403 Forbidden`):** Użytkownik próbujący wysłać żądanie modyfikacji jest tylko zwykłym uczestnikiem podróży, a nie jej twórcą. Backend odrzuca próbę.
* **Jednoczesna edycja (Concurrency / `409 Conflict`):** Jeśli w przyszłości aplikacja pozwoli na wielu "Ownerów", a dwóch spróbuje nadpisać dane w tym samym momencie, system może odrzucić jedną z prób informując o nieaktualnych danych (tzw. Optimistic Locking).
* **Błędne nowe dane (`400 Bad Request`):** Użytkownik usunął nazwę wycieczki całkowicie lub podał błędne ramy czasowe.

### Warunki wyjściowe (Postconditions)
* Dane wycieczki w bazie uległy modyfikacji.
* Wszyscy inni uczestnicy wycieczki zobaczą zaktualizowane dane po najbliższym odświeżeniu widoku w swoich aplikacjach.
