# WorkoutBase

Aplikacja Android do prowadzenia treningów interwałowych: przygotowanie, rundy pracy i przerwy,
sygnały dźwiękowe i wibracyjne, presety treningów oraz timer działający przy zgaszonym ekranie.

Kotlin, Jetpack Compose, jeden moduł `app`.

## Komendy

Testy jednostkowe:

```sh
./gradlew :app:testDebugUnitTest
```

Pełne sprawdzenie (lint Androida + testy jednostkowe):

```sh
./gradlew check
```

Hook blokujący commity bez dowodu — włącz raz na klon:

```sh
git config core.hooksPath .githooks
```

## Struktura

| Katalog | Zawartość |
| --- | --- |
| `timer/` | Czysty rdzeń timera (`TimerEngine`, `TimerFormat`), kontroler i adapter dźwięku |
| `data/` | Zapis konfiguracji i presetów w DataStore |
| `ui/` | Ekrany Compose |
| `service/` | Foreground service i powiadomienie |
| `app/src/test/` | Testy jednostkowe rdzenia |

## Zasady

Reguły pisania kodu są w osobnym repozytorium:
[ai_stuff/docs/zasady-ai.md](https://github.com/JonasWit/ai_stuff/blob/main/docs/zasady-ai.md).
Skrót dla tego projektu: `AGENTS.md`.

## Znany dług techniczny

Wymieniony w `AGENTS.md`, w sekcji „Known debt”. To lista rzeczy do naprawy, nie opis
docelowej architektury — nie naprawiaj ich przy okazji innych zmian.

## Licencja

Projekt prywatny, wszystkie prawa zastrzeżone.
