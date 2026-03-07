# Module 0: Setup

* **Estimated Complexity:** S (~5 files)
* **Estimated Files:** ~8
* **Key Risks:** None. Standard project initialization.

## Requirements
* Initialize a standard Android project (Pure Kotlin, Jetpack Compose).
* Set up global dependencies (Retrofit, Room, ExoPlayer, Compose, Navigation).
* Configure the base application theme according to the PRD (Black background, Neon Red/Blue accents).
* Setup base Room Database framework (Entity definitions without complex queries yet).

## UI Structure
* No user-facing screens in this module.
* `Theme.kt`: Define `Color.Black` as background everywhere, `Color(0xFF204E)` (Neon Red) and `Color(0x00E5FF)` (Electric Blue) as primary/secondary.

## Data & API
* `AppDatabase`: Room Database class.
* Entities matching Global Data Model from `modules.md`: `AlarmEntity`, `FavoriteStationEntity`.

## Technical Implementation
* Build.gradle (app): Include Core KTX, Lifecycle Runtime, Activity Compose, Compose BOM, Room (with KSP), Retrofit, OkHttp Logging Interceptor, Media3 (ExoPlayer).
* Application class (`HotBellApp`): Initialize logging or basic DB instance.

## Testing
* **Check 1:** Run `./gradlew build` - Build passes without errors.
* **Check 2:** App launches to a default empty Compose activity with a black background and doesn't crash.
