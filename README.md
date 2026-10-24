# WorkoutBase

Aplikacja Android do prowadzenia treningów interwałowych: przygotowanie, rundy pracy i przerwy,
sygnały dźwiękowe i wibracyjne, presety treningów oraz timer działający przy zgaszonym ekranie.

Kotlin, Jetpack Compose, jeden moduł `app`.

## Pętla pracy

Szybka pętla podczas pisania kodu — tylko testy jednostkowe, bez statycznej analizy (kilkanaście sekund):

```sh
./gradlew fastTest --offline
```

Pełne sprawdzenie przed commitem — ktlint, detekt, Android Lint i testy:

```sh
./gradlew check --offline
```
Testy UI działają w `fastTest` przez Robolectric — bez emulatora. Emulator uruchamiaj tylko wtedy, gdy trzeba zobaczyć wygląd; nie uruchamiaj testów instrumentalnych na telefonie z danymi użytkownika.

## Hook

Hook blokujący commity bez dowodu — włącz raz na klon:

```sh
git config core.hooksPath .githooks
```

## Wersjonowanie

- `versionName` jest semantyczny: `major.minor.patch` — pilnuje tego test.
- **Każda zmiana podbija wersję w tym samym commicie.** `versionCode` zawsze +1, `versionName` jako `major.minor.patch`: patch przy poprawkach, minor przy nowym zachowaniu, major przy zmianie niekompatybilnej. Wersja nigdy nie maleje i nie jest reużywana.
- Wersja jest pokazywana pod tytułem na ekranie głównym i pochodzi z builda (`BuildConfig`), więc nie rozjeżdża się z manifestem.
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

MIT — szczegóły w pliku [`LICENSE`](LICENSE).
