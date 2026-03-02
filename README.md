# ✈️ Trippy – Group Travel & Expense Manager

## Zespół
```
- Oleksii Nawrocki
- Tomasz Nowak
- Jakub Czesnak
- Dawid Bajek
- Mateusz Tokarz
```

## 📖 Opis projektu
**Trippy** to kompleksowa aplikacja mobilna (Android) oparta o architekturę klient-serwer, stworzona z myślą o osobach podróżujących w grupach. Rozwiązuje problem transparentnego zarządzania budżetem i sprawiedliwego rozliczania kosztów podczas wspólnych wyjazdów. 

Zamiast żmudnego wpisywania wydatków do arkuszy kalkulacyjnych, Trippy oferuje intuicyjny interfejs, w którym uczestnicy mogą na bieżąco dodawać koszty, określać kto za nie zapłacił i kogo dotyczą. Zaawansowany silnik backendowy dba o bezpieczeństwo danych, przeliczanie bilansów i synchronizację między użytkownikami.

Projekt realizowany jest w architekturze mikrousługowej/monolitycznej z wykorzystaniem konteneryzacji, co ułatwia wdrożenie i utrzymanie spójnego środowiska deweloperskiego.

### 🛠️ Stack Technologiczny
* **Frontend (Mobile):** Android (Kotlin, Jetpack Compose)
* **Backend:** Java, Spring Boot, Spring Security
* **Baza Danych:** PostgreSQL, Flyway / Liquibase
* **Infrastruktura:** Docker, Docker Compose

---

## 🚀 MVP (Minimum Viable Product)
Celem MVP jest dostarczenie podstawowego przepływu pracy – od rejestracji, poprzez utworzenie wyjazdu i dodanie wydatków, aż po pokazanie prostego bilansu uczestników. Wersja ta zakłada równy podział kosztów i operowanie na jednej walucie.

---

## ⚙️ Wymagania
Poniżej znajduje się specyfikacja głównych wymagań systemu w fazie MVP.

### Wymagania Funkcjonalne (Functional Requirements)
1. **Zarządzanie kontem (Autoryzacja):** Użytkownik musi mieć możliwość rejestracji i logowania przy użyciu adresu email i hasła (zabezpieczone tokenem JWT).
2. **Tworzenie podróży (Trips):** Zalogowany użytkownik może utworzyć nową podróż (podając jej nazwę oraz ramy czasowe), a także przeglądać listę swoich wyjazdów.
3. **Zarządzanie uczestnikami:** Twórca podróży (Owner) ma możliwość dodawania innych zarejestrowanych w systemie użytkowników do danego wyjazdu.
4. **Ewidencja wydatków (Expenses):** Każdy uczestnik podróży może dodać nowy wydatek, podając kwotę, tytuł wydatku (np. "Paliwo") oraz oznaczając osobę, która założyła pieniądze.
5. **Podgląd bilansu (Balances):** System musi automatycznie obliczać i wyświetlać aktualny bilans dla każdego uczestnika (podział kosztów po równo), pokazując kto ma nadpłatę, a kto i ile jest winien grupie.

### Wymagania Niefunkcjonalne (Non-functional Requirements)
1. **Konteneryzacja środowiska:** Cały backend (aplikacja Spring Boot oraz baza PostgreSQL) musi być uruchamiany lokalnie za pomocą jednego polecenia, wykorzystując plik `docker-compose.yml`.
2. **Bezpieczeństwo danych:** Hasła użytkowników muszą być bezwzględnie haszowane w bazie danych (np. przy użyciu algorytmu BCrypt), a API chronione przed nieautoryzowanym dostępem.
3. **Responsywność i wydajność mobilna:** Aplikacja kliencka na Androida musi działać płynnie, poprawnie obsługiwać stany ładowania/błędów sieciowych i być kompatybilna z urządzeniami z systemem Android 8.0 (API 26) lub nowszym.

## Struktura projektu

```
trippy/
|- backend/
|- mobile/
|- database/
|- .env
|- .gitignore
|- docker-compose.yml
```
## TRIPPY - YOUR TRAVELING COMPANION
