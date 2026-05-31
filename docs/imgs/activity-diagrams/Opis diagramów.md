
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

## 5. Weryfikacja konta (e-mail)
**Aktor:** Gość (Aplikacja Mobilna / Skrzynka e-mail)
**Cel:** Aktywacja świeżo zarejestrowanego konta, by mogło się zalogować.

### Warunki wejściowe (Preconditions)
* Konto zostało założone (status `isVerified=false`).
* Backend wygenerował wpis w tabeli `verification_tokens` i wysłał wiadomość e-mail
  (na profilu `prod` przez SMTP, na `dev` zapis do `backend/logs/emails/`).
* Token ma określony czas ważności (domyślnie 24h).

### Opis przepływu głównego
1. Użytkownik otwiera wiadomość z linkiem aktywacyjnym.
2. Klika link `GET /api/auth/verify?token=<UUID>`.
3. Backend wyszukuje token w tabeli `verification_tokens`.
4. Sprawdza, czy token nie wygasł.
5. Ustawia `user.isVerified=true`, usuwa zużyty token.
6. Zwraca `200 OK`. Frontend pokazuje komunikat „Konto aktywne" i przekierowuje
   do ekranu logowania.

### Scenariusze alternatywne
* **Nieprawidłowy token (`400 Bad Request`):** Token nie istnieje w bazie –
  ktoś sklejił niewłaściwy link lub wpis został już zużyty.
* **Token wygasł (`400 Bad Request`):** Czas ważności minął. Wpis zostaje
  usunięty, użytkownik musi ponownie zarejestrować się lub poprosić o nowy link.
* **Konto już zweryfikowane:** Idempotentnie zwracana jest informacja, że nie ma
  potrzeby ponownej weryfikacji.

### Warunki wyjściowe (Postconditions)
* `user.isVerified=true`, token usunięty z bazy.
* Użytkownik może się zalogować (wcześniej `AuthenticationServiceLoginTest`
  rzucał `RuntimeException` dla `isVerified=false`).

---

## 6. Zapraszanie uczestnika (Owner)
**Aktor:** Użytkownik Zalogowany (Owner wycieczki)
**Cel:** Dodanie zarejestrowanego użytkownika do wycieczki na zasadzie zaproszenia
oczekującego na akceptację.

### Warunki wejściowe (Preconditions)
* Użytkownik jest pomyślnie zalogowany i posiada ważny JWT.
* Aktor pełni rolę `Owner` na danej wycieczce.
* Zapraszany jest zarejestrowanym i zweryfikowanym użytkownikiem systemu.

### Opis przepływu głównego
1. Owner wchodzi w szczegóły wycieczki i wybiera „Dodaj uczestnika".
2. Wpisuje e-mail lub wybiera użytkownika z listy.
3. Aplikacja wysyła `POST /api/trips/{id}/invite` z tokenem JWT.
4. Backend weryfikuje JWT i rolę Owner.
5. Wyszukuje zapraszanego po e-mailu, sprawdza czy już nie jest uczestnikiem.
6. Zapisuje rekord w `trip_participants` z flagą `isAccepted=false`.
7. Zwraca `201 Created`. Frontend pokazuje „Zaproszenie wysłane".

### Scenariusze alternatywne
* **Brak uprawnień (`403 Forbidden`):** Caller nie jest Ownerem wycieczki.
* **Użytkownik nie istnieje (`404 Not Found`):** Brak konta o podanym e-mailu.
* **Już jest uczestnikiem (`409 Conflict`):** Wpis w `trip_participants`
  już istnieje (zaakceptowany lub oczekujący).

### Warunki wyjściowe (Postconditions)
* W tabeli `trip_participants` powstaje rekord `isAccepted=false`.
* Zapraszany widzi nową pozycję pod `GET /api/users/me/invitations`.

---

## 7. Akceptacja / odrzucenie zaproszenia
**Aktor:** Użytkownik Zalogowany (zapraszany)
**Cel:** Decyzja o dołączeniu lub odrzuceniu zaproszenia do wycieczki.

### Warunki wejściowe (Preconditions)
* Użytkownik jest zalogowany i ma ważny JWT.
* Istnieje wpis w `trip_participants` z `isAccepted=false`, gdzie `user_id`
  odpowiada zalogowanemu.

### Opis przepływu głównego
1. Użytkownik otwiera ekran „Zaproszenia" (`GET /api/users/me/invitations`).
2. Wybiera zaproszenie i decyduje:
   * **Akceptuje** → `POST /api/trips/{id}/accept`. Backend ustawia
     `isAccepted=true`. Wycieczka pojawia się w „Moje wyjazdy" i odtąd uczestnik
     bierze udział w rozliczeniu.
   * **Odrzuca** → `POST /api/trips/{id}/reject`. Backend usuwa rekord z
     `trip_participants`. Wycieczka znika z listy zaproszeń.
3. Backend zwraca `200 OK`.

### Scenariusze alternatywne
* **Zaproszenie nie istnieje (`404 Not Found`):** Owner cofnął zaproszenie
  (usunął uczestnika) zanim zapraszany zdążył odpowiedzieć.
* **Wycieczka usunięta:** Wycieczka została skasowana przez Ownera lub Admina –
  backend zwraca `404 Not Found`.

### Warunki wyjściowe (Postconditions)
* Akceptacja: rekord `trip_participants` ma `isAccepted=true`, uczestnik
  uwzględniany w bilansach (`Settlement`).
* Odrzucenie: rekord usunięty z `trip_participants`.

---

## 8. Rozliczanie długów (Settlement)
**Aktor:** Użytkownik Zalogowany (zaakceptowany uczestnik wycieczki)
**Cel:** Otrzymanie zminimalizowanej listy przelewów wyrównujących bilanse
wszystkich uczestników do zera.

### Warunki wejściowe (Preconditions)
* Użytkownik jest zalogowany, ma ważny JWT.
* Jest zaakceptowanym uczestnikiem wycieczki (`trip_participants.isAccepted=true`).
* Na wycieczce istnieją wydatki (`trip_node`).

### Opis przepływu głównego
1. Użytkownik wybiera „Pokaż bilans grupy" na ekranie wycieczki.
2. Aplikacja wysyła `GET /api/trips/{tripId}/balances` z tokenem JWT.
3. Backend weryfikuje JWT oraz członkostwo w wycieczce.
4. Pobiera wydatki współdzielone (`isSeparate=false`) i wylicza netto-bilans
   każdego zaakceptowanego uczestnika.
5. Uruchamia algorytm rozliczenia:
   * Zawsze: **Greedy** (Simplified Debt Graph) – max-heap kredytorów,
     min-heap dłużników, gwarancja ≤ N−1 przelewów.
   * Dodatkowo dla N ≤ 15 uczestników: **DP + bitmask** (Optimal Account
     Balancing) – partycja bilansów na maksymalną liczbę zero-sum subsetów.
   * Zwracany jest wynik z mniejszą liczbą przelewów.
6. Zwraca `200 OK` z listą przelewów (kto → komu → kwota) i sumarycznym bilansem.

### Scenariusze alternatywne
* **Token nieważny (`401 Unauthorized`):** Frontend wylogowuje użytkownika.
* **Caller spoza wycieczki (`403 Forbidden`):** Użytkownik nie jest
  zaakceptowanym uczestnikiem.
* **Brak wydatków:** Lista przelewów pusta, bilanse uczestników = 0.
* **N > 15:** DP się nie uruchamia (eksplozja 3^N), wynik z greedy.

### Warunki wyjściowe (Postconditions)
* Stan bazy danych pozostaje **niezmieniony** – bilans liczony jest „na żywo".
* Frontend wyświetla bilans i listę przelewów.

> **Uwaga (Future work, v1.1):** Planowane jest ręczne „zamknięcie" wycieczki
> przez Ownera z zapisem snapshotu rozliczenia w `TripSettlementSnapshot`.
> Wtedy bilans przestaje być liczony na żywo.

---

## 9. Akcje administratora
**Aktor:** Admin (Panel administracyjny Vue)
**Cel:** Zarządzanie systemem – użytkownikami, wycieczkami, moderacja treści
i konfiguracja słowników.

### Warunki wejściowe (Preconditions)
* Użytkownik posiada konto z rolą `ADMIN` (np. `admin@trippy.pl`).
* Jest zalogowany w panelu administracyjnym (Vue 3, `:5173` w dev,
  reverse-proxy nginx w prod).

### Opis przepływu głównego
Admin wybiera sekcję panelu i wykonuje jedną z operacji:

**A) Zarządzanie użytkownikami (`/api/admin/users`)**
1. Lista wszystkich userów (`GET`).
2. Edycja danych usera (`PUT /api/admin/users/{id}`) – np. ustawienie
   `isBlocked=true` blokuje konto.
3. Tworzenie nowego usera (`POST`) lub usunięcie (`DELETE`).
4. Podmiana zdjęcia profilowego (`POST .../{id}/photo`, walidacja MIME Tika).

**B) CRUD wycieczek (`/api/admin/trips`)**
1. Lista wszystkich wycieczek w systemie (`GET`).
2. Podgląd szczegółów dowolnej wycieczki (`GET /{id}`).
3. (Future) edycja / usunięcie wycieczki w przypadku łamania regulaminu.

**C) Moderacja treści (`/api/admin/moderation`)**
1. Lista zgłoszonych / wszystkich postów (`GET /posts`).
2. Usunięcie postu (`DELETE /posts/{id}`).
3. Lista zdjęć (`GET /photos`), usunięcie zdjęcia (`DELETE /photos/{id}`).

**D) Zarządzanie słownikami (`/api/admin/dictionaries`)**
1. Waluty: lista i tworzenie (`GET/POST /currencies`).
2. Role wyjazdowe (`TRIP_ROLE`): lista i tworzenie (`GET/POST /roles`).

Każde żądanie przechodzi przez `JwtAuthenticationFilter`, który weryfikuje
token JWT oraz autorytet `ROLE_ADMIN` (Spring Security). Brak uprawnień →
`403 Forbidden`.

### Scenariusze alternatywne
* **Token nieważny lub brak roli ADMIN (`401`/`403`):** Panel wylogowuje.
* **Próba usunięcia nieistniejącego zasobu (`404`):** Komunikat „Element
  nie znaleziony".
* **Naruszenie integralności bazy (`409 Conflict`):** Np. próba usunięcia
  waluty używanej w aktywnych wycieczkach.

### Warunki wyjściowe (Postconditions)
* W zależności od operacji: zaktualizowane / utworzone / usunięte rekordy w DB.
* Operacja jest audytowalna w logach backendu.
* Zablokowany user (`isBlocked=true`) nie może się zalogować ani odświeżyć tokenu.
