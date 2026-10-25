# WorkoutBase

An interval training timer for Android: prepare, work and rest rounds, sound and
vibration cues, saved workouts and screen-off operation.

## Features

**Interval timer**

- phases: prepare, work, rest and finish, with automatic transition to the next round
- configurable number of rounds and work, rest and prepare durations
- 3-2-1 countdown before each round, can be turned off
- half-way cue during the work round, can be turned off
- start, pause, resume, reset and skip phase
- quick 15-second extension and shortening of the current phase

**During a workout**

- large countdown readable from several meters away
- progress ring for the current phase
- segmented bar for the whole session, split into prepare, work and rest
- animated flame background whose color and speed follow the phase
- sounds and vibration for work start, round end, half-way and workout end
- the screen stays on during a session
- the timer keeps running with the screen off, with a notification showing the remaining time

**Saved workouts**

- save the current configuration as a named workout
- up to 20 saved workouts
- preview parameters, load, edit and delete
- export all workouts to a JSON file and import from a file
- import reports how many workouts were added and how many were skipped because of the limit

**Interface**

- four screens: timer, settings, workout list and workout editor
- all text available in Polish and English in the source string resources
- version number shown under the title on the main screen

## Installation

Download `app-release.apk` from the
[Releases](https://github.com/JonasWit/workoutbase/releases) tab and open it on your
phone. Android may ask for permission to install from an unknown source.

If you have an older version signed with a different key, the system will refuse the
update. In that case: `Trainings` → `EXPORT`, uninstall the old version, install the
new one, `IMPORT`.

## Getting started

1. Open `Settings`, set the rounds and the work, rest and prepare durations, then save.
2. On the timer screen, press start.
3. To keep the configuration, open `Trainings` and save it under a name.

## Data and privacy

All data stays on the device. The app has no account, no login and no network
connection. Workouts are stored locally, and the only way to move them is a manual
export to a JSON file.

## License

MIT — see [`LICENSE`](LICENSE) for details.
