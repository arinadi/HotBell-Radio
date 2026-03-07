# HotBell Radio

A modern Android alarm clock application that wakes you up to your favorite internet radio stations. 

If you are a deep sleeper, traditional alarms might not be enough. HotBell ensures you are fully awake by requiring you to solve a math challenge before you can dismiss the alarm. As you try to solve the quiz, the radio plays in the background.

## Features

- **Radio Alarms**: Wake up to live internet radio from all around the world, powered by the [Radio Browser API](https://www.radio-browser.info/).
- **Wake-Up Challenge**: Stop the alarm by holding down the correct answer to a randomly generated math problem.
- **Gradual Volume Crescendo**: Don't get startled! Alarms start softly at 10% volume and gradually increase to full volume. If you don't wake up within a minute, the app uses a *Loudness Enhancer* to boost the volume up to 150%.
- **Interactive Feedback**: When solving the wake-up puzzle, wrong answers are met with strong haptic vibrations, a red flashing screen, and a shaking animation to stimulate your brain.
- **Fail-safe Fallback Ringtones**: If there is no internet connection or the selected radio station fails to play within 10 seconds, the app plays a built-in alarm ringtone.
- **Modern UI**: Full Dark Mode design with vibrant "Neon Red" and "Electric Blue" accents built purely using Jetpack Compose.
- **Battery & Doze Optimized**: Uses exact alarms (`AlarmManager.setAlarmClock()`) and Full-Screen Intents (Android 10+) to ensure unreliable doze states don't skip your alarm. Android 14 full-screen intent limitations are handled seamlessly via an in-app permission manager.

## Technologies Used

- **UI Framework:** Jetpack Compose, Material 3
- **Local Persistence:** Room Database
- **Media Playback:** ExoPlayer (Media3 Session API)
- **Networking:** Retrofit, OkHttp, GSON
- **Asynchronous Processing:** Kotlin Coroutines & Flow
- **Background Tasks:** Foreground Services (`MediaSessionService`), Broadcast Receivers, `AlarmManager`

## Requirements
- Android SDK 26 (Android 8.0) to Android SDK 34 (Android 14)

## Getting Started

1. Clone this repository.
2. Open the project in Android Studio.
3. Sync the Gradle files.
4. Run the application on a physical device or emulator. (Note: Accurate alarm testing is best performed on physical devices due to variable Android Doze implementations on emulators).

## Setting up Permissions

Make sure you grant the required permissions when prompted:
* **Notifications**: To view the active media playback service.
* **Exact Alarms**: Required for Android 12+ to schedule wake-up events precisely.
* **Full-Screen Intents**: Required for Android 14+ to wake up the screen from sleep cleanly when the alarm triggers.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
