# WorkoutBase

Interwałowy timer treningowy na Androida: przygotowanie, rundy pracy i przerwy, sygnały
dźwiękowe i wibracyjne, zapisane treningi oraz praca przy zgaszonym ekranie.

## Funkcje

**Timer interwałowy**

- fazy: przygotowanie, praca, przerwa i zakończenie, z automatycznym przejściem do kolejnej rundy
- konfiguracja liczby rund oraz długości pracy, przerwy i przygotowania
- odliczanie 3-2-1 przed każdą rundą, z możliwością wyłączenia
- sygnał w połowie rundy pracy, z możliwością wyłączenia
- start, pauza, wznowienie, reset i pomijanie fazy
- szybkie wydłużanie i skracanie bieżącej fazy o 15 sekund

**Podczas treningu**

- duże odliczanie czytelne z kilku metrów
- pierścień postępu bieżącej fazy
- segmentowy pasek całej sesji z podziałem na przygotowanie, pracę i przerwy
- animowane tło płomieni, którego kolor i tempo zależą od fazy
- dźwięki i wibracje dla startu pracy, końca rundy, połowy i zakończenia treningu
- ekran nie gaśnie w trakcie sesji
- timer działa dalej przy zgaszonym ekranie, z powiadomieniem pokazującym pozostały czas

**Zapisane treningi**

- zapisanie bieżącej konfiguracji jako trening z nazwą
- do 20 zapisanych treningów
- podgląd parametrów, wczytanie, edycja i usuwanie
- eksport wszystkich treningów do pliku JSON i import z pliku
- import pokazuje, ile treningów dodano, a ile pominięto z powodu limitu

**Interfejs**

- cztery ekrany: timer, ustawienia, lista treningów i edycja treningu
- wszystkie teksty po polsku i angielsku w kodzie źródłowym zasobów
- numer wersji widoczny pod tytułem na ekranie głównym

## Instalacja

Pobierz plik `app-release.apk` z zakładki
[Releases](https://github.com/JonasWit/workoutbase/releases) i otwórz go na telefonie.
Android może poprosić o zgodę na instalację z nieznanego źródła.

Jeśli masz zainstalowaną starszą wersję podpisaną innym kluczem, system odmówi aktualizacji.
Wtedy: `Trainings` → `EXPORT`, odinstalowanie starej wersji, instalacja nowej, `IMPORT`.

## Pierwsze kroki

1. Wejdź w `Settings`, ustaw rundy oraz czasy pracy, przerwy i przygotowania, zapisz.
2. Na ekranie timera naciśnij start.
3. Aby zachować konfigurację, wejdź w `Trainings` i zapisz ją pod nazwą.

## Dane i prywatność

Wszystkie dane zostają na urządzeniu. Aplikacja nie ma konta, logowania ani połączenia z siecią.
Treningi są przechowywane lokalnie, a jedynym sposobem ich przeniesienia jest ręczny eksport
do pliku JSON.

## Licencja

MIT — szczegóły w pliku [`LICENSE`](LICENSE).
