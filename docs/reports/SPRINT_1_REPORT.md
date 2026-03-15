# Raport z Postępów: Sprint 1

Projekt: **Trippy**<br>
Data: **15.03.2026**<br>
Cel Sprintu: Skonfigurowanie środowiska deweloperskiego i repozytorium, przygotowanie wymaganej dokumentacji projektowej (diagramy UML, README) oraz dostarczenie działającego mechanizmu autoryzacji użytkowników (rejestracja i logowanie JWT).

## 1. Statystyki Repozytorium

W trakcie trwania Sprintu 1 zespół zrealizował ponad wymagane 15 commitów, pracując w modelu gałęzi funkcyjnych.

Podział prac:
![Contributions](docs/imgs/reports/report_1/contributions.png)

- Zespół zintegrował kod za pomocą Pull Requestów.

- Wdrożono zasady nazewnictwa gałęzi oraz commitów zgodnie z kluczami zadań w systemie Jira.

## 2. Proces Code Review (Kluczowy element oceny)

Zgodnie z przyjętym w zespole procesem, żaden kod nie trafił do głównej gałęzi (develop/main) bez utworzenia Pull Requesta i sprawdzenia go przez innego członka zespołu.

Przykłady wartościowych uwag z Code Review przeprowadzonych w Sprincie 1:

### Kwestia Bezpieczeństwa:

**Sytuacja**: W jednym z pierwszych PR-ów dotyczących serwisu e-mail, dane logowania do serwera SMTP zostały zahardcodowane bezpośrednio w pliku `application.properties`.

**Rozwiązanie**: Kod został cofnięty. Autor przeniósł sekrety do zabezpieczonego pliku .env, wykluczonego z repozytorium, a do środowiska Dockerowego wstrzyknięto je przez zmienne środowiskowe. Dodatkowo skompromitowane hasła zostały unieważnione.

### Logika Biznesow:

Sytuacja: Napisano endpoint rejestracji generujący token JWT.

**Reakcja w Code Review**: Zauważono brak walidacji istniejącego użytkownika w bazie. Rejestracja drugiego użytkownika na ten sam e-mail powodowała błąd bazy danych.

**Rozwiązanie**: Dodano obsługę wyjątku na poziomie AuthenticationService oraz napisano precyzyjne testy jednostkowe, sprawdzające blokadę duplikatów oraz poprawność szyfrowania haseł.

## 3. Zrealizowane Zadania

Wszystkie poniższe zadania przeszły przez proces Code Review i zostały pomyślnie zintegrowane.

### Backend & Infrastruktura:

- Inicjalizacja projektu Spring Boot + konfiguracja bazy PostgreSQL uruchamianej w kontenerze Docker.

- Implementacja bezpiecznego systemu uwierzytelniania opartego na tokenach JWT.

- Mechanizm rejestracji użytkowników wraz z szyfrowaniem haseł.

- System weryfikacji konta poprzez e-mail.

- Napisanie testów jednostkowych dla modułu autoryzacji z uwzględnieniem "edge cases".

### Organizacja i DevOps:

- Konfiguracja automatyzacji w systemie Jira. Zintegrowanie tablicy z repozytorium GitHub.

### Frontend:

- Implementacja widoków logowania i rejestracji.

- Zaimplementowanie biblioteki Retrofit, podpięcie pod API oraz stworzenie interceptora JWT.

- Podpięcie widoków do logiki biznesowej aplikacji.

## 4. Wnioski i Plany na Sprint 2

Po zakończeniu pierwszej iteracji zespół zdecydował się na profesjonalny podział ról w architekturze klient-serwer:

- Backend Team (2 osoby): Skupi się na tworzeniu logiki biznesowej w Spring Boot, projektowaniu skomplikowanych relacji bazodanowych w Hibernate oraz wystawieniu dokumentacji API.

- Mobile Team (3 osoby): Przejmie pełną odpowiedzialność za stworzenie interfejsu aplikacji na system Android, konsumpcję API oraz obsługę lokalnej bazy danych.

Podział ten pozwoli na równoległą pracę i wyeliminuje ryzyko konfliktów przy scalaniu kodu.