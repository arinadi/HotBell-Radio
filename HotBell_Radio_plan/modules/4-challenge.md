# Module 4: Wake-up Challenge

* **Estimated Complexity:** S (~7 files)
* **Estimated Files:** ~8
* **Key Risks:** Bypassing the Android Lockscreen reliably and handling sudden network disconnects during the alarm.

## Requirements
* A full-screen `WakeUpActivity` that appears over the lock screen and turns the screen on.
* Automatically buffers and plays the alarm's radio station using `RadioRepository` (Module 1).
* Displays a random 4-choice cognitive quiz (e.g., Simple Math).
* Implements a "Hold-to-Confirm" button behavior requiring exactly 3 seconds of continuous pressing on the correct answer to dismiss the alarm.
* Fallback to a local obnoxious ringtone if the radio stream fails to buffer within 3 seconds.

## UI Structure
* Large bold time at the top.
* Station Name/Metadata in the middle.
* 4 big buttons for the multiple-choice quiz.
* Visual progress bar or fill animation inside the button during the 3-second hold.

## Data & API
* Consumes `Intent` extras (Alarm ID, Station URL).
* No new DB tables.

## Technical Implementation
* `WakeUpActivity` must use `setShowWhenLocked(true)`, `setTurnScreenOn(true)`, and `window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)`.
* Quiz generator utility (e.g., `ArithmeticQuizGenerator` returning `{ question: "7 + 5", answers: ["11", "12", "13", "14"], correctIndex: 1 }`).
* Compose `pointerInput { detectTapGestures(onPress = { ... }) }` to track the 3-second hold. Only the correct answer's button responds.
* `Handler` or `Coroutine` timeout: If player state is not `Playing` after 3000ms, start Android `RingtoneManager` fallback.

## Testing
* **Check 1:** Activity starts, turns on the screen, and bypasses the keyguard.
* **Check 2:** Radio starts playing automatically.
* **Check 3:** Tapping or holding incorrect answers does nothing. Tapping briefly on the correct answer does nothing.
* **Check 4:** Holding the correct answer for 3 seconds stops playback, cancels the notification, and finishes the Activity.
* **Check 5:** Simulating network failure triggers the fallback local ringtone after 3 seconds.
