# Agent Context: HotBell Radio

You are working on **HotBell Radio**, a modern Android native alarm clock application written purely in Kotlin. 

## 1. Project Overview
HotBell Radio solves the problem of heavy sleepers getting used to standard alarm ringtones. It combines unpredictable live internet radio streams with a cognitive task (a multiple-choice math quiz) and a physical action (hold-to-confirm gesture for 3 seconds) to ensure the user is fully awake before the alarm can be dismissed.

- **Primary Philosophy:** "A little bit different > a little bit better." Always design with a mobile-first philosophy.
- **Core Features:** Dual alarm/radio player, internet radio integration, cognitive dismissal quiz, hold-to-confirm gesture, fail-safe ringtones, gradual volume crescendo, global **Now Playing Bar**, and intensive haptic/visual feedback.
- **Branding:** Centered around the "H." logo (white H with a HotBell Orange dot).

## 2. Technology Stack
- **Language:** Kotlin 1.9.x (Pure Kotlin)
- **UI Framework:** Jetpack Compose (Material 3)
- **Minimum SDK:** 26 (Android 8.0) | **Target SDK:** 34 (Android 14)
- **Local Database:** Room Database (version `2.6.x`)
- **Networking:** Retrofit 2 + OkHttp (`2.11.x`)
- **Media Playback:** ExoPlayer via AndroidX Media3 (`1.3.x`)
- **Background Execution:** `AlarmManager` (Exact Alarms), WorkManager, Foreground Services (`MediaSessionService`).

## 3. Architecture & Code Conventions
- **Architecture Pattern:** MVVM (Model-View-ViewModel) with Unidirectional Data Flow using Kotlin Coroutines and StateFlow.
- **Structure:** Standard Android package layout under `com.hotbell.radio`. Grouped by feature (`alarms`, `player`, `ui`, `data`).
- **Compose Rules:** Screen files must be suffixed with `Screen.kt` (e.g., `WakeUpScreen.kt`). 
- **Modifiable Files:** **NEVER** modify `PRD.md`, `modules.md`, or `agent_prompt.md` within the `HotBell_Radio_plan/` directory. These are read-only blueprints.

## 4. UI/UX & Design Rules
- **Aesthetics & Theme:** Full Dark Mode. 
    - Background: Pitch Black (`#000000`) to save OLED battery when resting.
    - Accents: Neon Red (`#FF204E`) and Electric Blue (`#00E5FF`).
- **Typography:** Bold, highly legible sans-serif (Inter/Roboto), with huge vertically stacked digits for the Alarm Edit screen.
- **Interactions:** Large tap targets (min 48dp). The hold-to-confirm interaction relies heavily on visual fill animations, red screen flashes for wrong answers, and intense haptic feedback (3-second vibrations).
- **Global Components:** The `NowPlayingBar` must remain accessible above the bottom navigation across all primary screens.

## 5. Critical System Behaviors
- **Alarm Reliability:** Must bypass Doze mode. Use exact alarms (`AlarmManager.setAlarmClock`) and full-screen intents.
- **Permissions:** Android 14+ requires explicit `SCHEDULE_EXACT_ALARM`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`, and Full-Screen Intent permissions. These must be checked and requested gracefully via the Settings screen.
- **Playback Fallback:** If internet is down or the radio stream takes more than 3 seconds to buffer, the app **must** fall back immediately to a built-in ringtone to guarantee the user wakes up.

## 6. Development Workflow & Git Strategy
- **Validation:** Always verify with `./gradlew assembleDebug` and `./gradlew lintDebug`. Code must compile and run successfully before committing.
- **Git Commit Format:** `feat(module-[name/number]): [description]` or `fix(module-[name/number]): [description]`.
- **Error Recovery:** Attempt to fix up to 3 times. If unresolved, stash or hard reset to the last working commit and notify the user. Never proceed with a broken build.
- **Logging:** Document major steps and new dependencies in `execution_log.md` and update `progress.json`.

**Keep this context in mind for every feature implementation, refactor, or bug fix for HotBell Radio.**
