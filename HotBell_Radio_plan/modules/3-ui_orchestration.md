# Module 3: UI & Orchestration

* **Estimated Complexity:** L (~15 files)
* **Estimated Files:** ~20
* **Key Risks:** State management complexity across Dual Functionality (Alarm settings vs Music playing).

## Requirements
* **Home Screen:** List of alarms (Toggle on/off, tap to edit). Fab to add new alarm. "Explore Radio" button.
* **Explore Radio Screen:** Search bar, list of stations. Play/Pause button for each. Star icon to favorite/unfavorite.
* **Add/Edit Alarm Screen:** Time picker, Day selector, Station selector (reuses Explore Radio UI but returns result instead of just playing).
* Permission handling for Exact Alarms and Notifications (Android 13+).

## UI Structure
* **Navigation:** Jetpack Type-Safe Compose Navigation.
* **HomeRoute:** Displays `Flow<List<Alarm>>`.
* **RadioExplorerRoute:** Displays Search Input, `LazyColumn` of `StationNetworkModel` or `FavoriteStationEntity`. Exposes Player controls at the bottom if audio is playing in the background.
* **AlarmEditRoute:** Time input, Day chips, selected Station display.

## Data & API
* Consumes `AlarmDao` and `RadioRepository`.

## Technical Implementation
* Use MVI/MVVM pattern with `ViewModel` to hold state.
* The "Explore Radio" screen needs an argument mode: `Mode.GeneralListening` vs `Mode.SelectForAlarm`. If `SelectForAlarm`, tapping a station returns the station data to the `AlarmEditRoute` via Navigation `savedStateHandle`.
* `RequestPermission` accompanist or standard `ActivityResultContracts` for exact alarms and POST_NOTIFICATIONS.

## Testing
* **Check 1:** Adding a new alarm saves it to the DB and appears on the Home Screen.
* **Check 2:** Toggling an alarm on the Home Screen updates the DB and calls `AlarmScheduler`.
* **Check 3:** "Explore Radio" successfully searches, plays a station, and can add it to room DB as a favorite.
* **Check 4:** Editing an alarm and selecting a station properly associates the station UUID with the alarm in the DB.
