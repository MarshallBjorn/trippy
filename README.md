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

## System Kontroli Wersji i Strategia Odgałęzień (Branching Strategy)

W celu zapewnienia spójności, stabilności bazy kodu oraz ujednolicenia procesu integracji, zespół projektowy stosuje rygorystyczne zasady pracy z systemem Git. Proces oparty jest na zdefiniowanej hierarchii gałęzi (branches) oraz obowiązkowych przeglądach kodu (Code Review).

### 1. Gałęzie Główne (Long-lived branches)
Architektura repozytorium opiera się na dwóch głównych gałęziach o nieograniczonym cyklu życia:

* **`main`** – Gałąź produkcyjna. Zawiera wyłącznie kod przetestowany, stabilny i gotowy do wdrożenia (wersje wydań / MVP). Bezpośrednie wprowadzanie zmian (tzw. *direct commit*) do tej gałęzi jest surowo zabronione. Zmiany trafiają tu wyłącznie poprzez proces fuzji (merge).
* **`develop`** – Główna gałąź integracyjna. Agreguje zatwierdzone zmiany z gałęzi roboczych. Pełni funkcję bazy do tworzenia nowych odgałęzień i jest docelowym miejscem fuzji dla nowo zaimplementowanych funkcjonalności.

### 2. Konwencja Nazewnictwa Gałęzi (Short-lived branches)
Każde nowe zadanie (funkcjonalność, poprawka, dokumentacja) wymaga utworzenia dedykowanej, tymczasowej gałęzi roboczej. Nazewnictwo musi być zgodne z formatem `typ/krotki-opis-zadania`, gdzie opis zapisany jest w formacie *kebab-case* (małe litery, słowa oddzielone myślnikiem).

Dopuszczalne prefiksy (typy):
* **`feature/<nazwa>`** – Implementacja nowej funkcjonalności systemu (np. `feature/jwt-authentication`, `feature/expense-entity`). Gałąź tworzona z `develop`.
* **`bugfix/<nazwa>`** – Usunięcie błędu zidentyfikowanego w środowisku deweloperskim (np. `bugfix/balance-calculation-error`). Gałąź tworzona z `develop`.
* **`hotfix/<nazwa>`** – Krytyczna poprawka błędu na środowisku produkcyjnym. Tworzona bezpośrednio z gałęzi `main`. Po zakończeniu i weryfikacji prac, gałąź jest włączana (merge) zarówno do `main`, jak i do `develop` (np. `hotfix/database-connection-loss`).
* **`documentation/<nazwa>`** – Tworzenie, rozbudowa lub aktualizacja dokumentacji technicznej oraz plików konfiguracyjnych (np. `documentation/api-endpoints`, `documentation/readme-update`).
* **`refactor/<nazwa>`** – Restrukturyzacja i optymalizacja istniejącego kodu, niepociągająca za sobą zmian w jego obserwowalnym zachowaniu zewnętrznym (np. `refactor/user-service-architecture`).

### 3. Przepływ Pracy i Integracja Kodu (Pull Request Workflow)
Wprowadzanie zmian do głównej linii kodu podlega ustandaryzowanemu procesowi:

1. **Synchronizacja:** Pobranie aktualnego stanu gałęzi bazowej (`git pull origin develop`).
2. **Inicjalizacja:** Utworzenie nowej gałęzi roboczej zgodnie z konwencją nazewnictwa (`git checkout -b <typ>/<nazwa>`).
3. **Implementacja:** Wprowadzenie zmian, poprawne sformułowanie wiadomości *commitów* i ich lokalne zatwierdzenie.
4. **Publikacja:** Przesłanie zmian do zdalnego repozytorium (`git push origin <typ>/<nazwa>`).
5. **Żądanie integracji:** Utworzenie Pull Requesta (PR) do gałęzi docelowej (standardowo `develop`).
6. **Code Review:** Obowiązkowa weryfikacja jakości i poprawności kodu przeprowadzona przez minimum jednego, innego członka zespołu.
7. **Fuzja (Merge):** Włączenie kodu do gałęzi docelowej po uzyskaniu akceptacji roboczej (Approval) i pozytywnym przejściu ewentualnych testów zautomatyzowanych.

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

```
docker-compose up -d --build

docker-compose down -v
```

## TRIPPY - YOUR TRAVELING COMPANION
