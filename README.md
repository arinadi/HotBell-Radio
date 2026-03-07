# HotBell Radio

A modern Android alarm clock application that wakes you up to your favorite internet radio stations. 

If you are a deep sleeper, traditional alarms might not be enough. HotBell ensures you are fully awake by requiring you to solve a math challenge before you can dismiss the alarm. As you try to solve the quiz, the radio plays in the background.

## Features

- **Radio Alarms**: Wake up to live internet radio from all around the world, powered by the [Radio Browser API](https://www.radio-browser.info/).
- **Wake-Up Challenge**: Solve a math quiz with a hold-to-confirm gesture to ensure you're fully awake.
- **Gradual Volume Crescendo**: Alarms start at 10% volume and increase to 150% using a *Loudness Enhancer* for heavy sleepers.
- **Interactive Feedback**: Screen flashing, intensive haptic vibrations, and shaking animations for a stimulating wake-up experience.
- **Now Playing Bar**: A global playback control bar that persists across all screens.
- **Refined Alarm Planning**: Modern, mockup-aligned Alarm Edit screen with circular day selectors and inline time editing.
- **Branded Experience**: Custom "H." logo and consistent "HotBell Orange" accents throughout the app.
- **Reliability Suite**: Comprehensive permission management (Battery Optimization, Exact Alarms, Full-Screen Intents) to ensure 100% alarm reliability on Android 10-14+.

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

Make sure you grant the required permissions in the **Settings** screen:
* **Notifications**: To see the active playback and alarm status.
* **Battery Optimization**: Critical to prevent the system from killing the alarm service in the background.
* **Exact Alarms**: Required for Android 12+ for precision scheduling.
* **Full-Screen Intent**: Required for Android 14+ to show the wake-up screen while the device is locked.

## License

This project is licensed under the MIT License - see the LICENSE file for details.
