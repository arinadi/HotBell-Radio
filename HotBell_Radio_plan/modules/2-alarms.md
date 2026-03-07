# Module 2: Alarm Management

* **Estimated Complexity:** M (~10 files)
* **Estimated Files:** ~12
* **Key Risks:** `AlarmManager` reliability, Android 14+ `SCHEDULE_EXACT_ALARM` permission, and restoring alarms on boot.

## Requirements
* Room DB DAO for `AlarmEntity` (CRUD).
* `AlarmScheduler` wrapper around Android's `AlarmManager` to set, cancel, and update exact alarms.
* `BootReceiver` to reschedule all active alarms when the device reboots.
* `AlarmReceiver` (BroadcastReceiver) that triggers the `WakeUpActivity` (Module 4) when the alarm fires.

## UI Structure
* No user-facing Compose screens in this module.
* Exposes `Flow<List<AlarmEntity>>` from the DB for Module 3 to display.

## Data & API
* **Local DB:** `AlarmDao`
* **Intents:** 
    * `ACTION_SET_ALARM`
    * `ACTION_DISMISS_ALARM`
    * `ACTION_BOOT_COMPLETED`

## Technical Implementation
* **Alarm Scheduling:** Use `alarmManager.setExactAndAllowWhileIdle()` (or `setAlarmClock()` for overriding visual system UI if strictly necessary).
* **Permissions:** Add `<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />` and `<uses-permission android:name="android.permission.USE_EXACT_ALARM"/>` (if acceptable for Play Store policy) or handle the fallback intent to request user permission. Add `<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />`.
* **Broadcast Receiver:** `AlarmReceiver` must launch `WakeUpActivity` using an Intent with `Intent.FLAG_ACTIVITY_NEW_TASK` and `Intent.FLAG_ACTIVITY_CLEAR_TASK`.

## Testing
* **Check 1:** Creating an alarm inserts it into Room DB and calls `AlarmManager.setExactAndAllowWhileIdle`.
* **Check 2:** Sending a mock `ACTION_BOOT_COMPLETED` intent verifies `BootReceiver` queries the DB and reschedules active alarms.
* **Check 3:** `AlarmReceiver.onReceive` successfully launches `WakeUpActivity`.
