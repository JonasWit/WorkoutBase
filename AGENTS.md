# Agent Instructions

Android app (Kotlin, Jetpack Compose, single module `app`). The canonical coding rules
live in the knowledge repository:
<https://github.com/JonasWit/ai_stuff/blob/main/docs/zasady-ai.md>.
Read it before writing code. This file only records what is specific to this project.

## Verification

- Tests come first, always. The only exceptions are changes with no executable
  behavior: documentation, comments, commit messages, tool-enforced formatting.
- Watch the test fail before implementing. A test that passed before the change
  proves nothing.
- No claim without a test; otherwise say "unverified" and what is missing.
- Commands:
  - unit tests: `./gradlew :app:testDebugUnitTest`
  - full check (lint + unit tests): `./gradlew check`
- Every commit message needs a trailer line: `Test: <name>` or
  `Unverified: <reason>`. The `commit-msg` hook rejects anything else.
  Enable it once per clone: `git config core.hooksPath .githooks`

## Structure

- `timer/TimerEngine.kt`, `timer/TimerFormat.kt` — pure core: no Android imports, time
  passed in as a parameter, immutable state.
- `timer/TimerController.kt` — drives the engine with wall-clock time; app-scoped.
- `timer/AndroidCuePlayer.kt` — adapter for sound and vibration behind `CuePlayer`.
- `data/` — DataStore persistence for `TimerConfig` and presets.
- `ui/` — Compose screens.
- `service/` — foreground service and notification.

## Known debt (do not fix unless the task touches it)

- `AppGraph` is a mutable app-scoped holder and there are no ViewModels, so screens call
  it directly and the timer instance is not tied to app visibility. Target: controller
  owned by a ViewModel, dependencies passed explicitly.
- User-facing strings are hardcoded in Compose. Target: `strings.xml`.
- `TimerScreen.kt` mixes the timer UI with procedural flame rendering. Target: split
  by topic.
- No static analysis beyond Android Lint and no CI: `./gradlew check` and the
  `commit-msg` hook run only when someone runs them locally.

Fixed and removed from this list: `TimerController` now takes an injected clock and has
tests; configuration mapping lives in `ConfigPreferences`; duration conversion lives in
`Duration.kt`; cue behaviour lives in `cuePlan` with tests.

## Boundaries

- One change, one purpose. Report unrelated problems instead of fixing them in the
  same diff.
- New dependency needs a justification: what the standard library or Compose cannot do.
- Do not guess Android APIs. Check the version in `gradle/libs.versions.toml` first.
