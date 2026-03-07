# HotBell Radio

## Phase 0: Project Context
* **Project Type:** Greenfield
* **Project Mode:** `mobile` (Android - Pure Kotlin)
* **Existing Assets:** API: `https://all.api.radio-browser.info/`
* **Constraints:** Must be Pure Kotlin to keep the app small and support many devices.
* **Target Users:** Heavy sleepers who have become accustomed to standard ringtones and need mental and physical engagement to wake up effectively.

---

## Phase 1: Product Requirements Document (PRD)

### The Story (The Pain)
Many heavy sleepers struggle to wake up using standard alarm clock ringtones. Over time, the brain becomes accustomed to the repetitive sounds, making it dangerously easy to hit the "snooze" button on autopilot or sleep through the alarm entirely. Waking up requires a sudden jumpstart in cognitive engagement.

### Competitive Edge
**"A little bit different > a little bit better."**
Instead of merely solving mathematical equations (which many competitors like Alarmy do) or playing a jarring sound, HotBell Radio combines live, unpredictable audio with a forced cognitive task. When the alarm triggers, it plays an unpredictable live stream from an online radio station. To turn it off, users must complete a 4-choice multiple-choice quiz and **physically press and hold** the correct answer for 3 seconds, complete with a visual progress animation. This engages hearing (live radio), cognition (quiz), and motor control (hold gesture)—guaranteeing the user is awake.

### Core Features (No Capes!)
*   **Dual Functionality (Alarm & Radio Player):** Serves as a reliable daily alarm and a standalone online radio player.
*   **Online Radio Integration:** Streams live radio from `radio-browser.info`. Users can explore, listen to, and favorite stations independent of alarms.
*   **Cognitive Dismissal Quiz:** A simple 4-choice multiple-choice quiz appears when the alarm rings.
*   **Hold-to-Confirm Gesture:** The alarm only dismisses if the user presses and holds the correct quiz answer for exactly 3 seconds, accompanied by a visual fill animation.

### Base Features (CRUD)
*   **Alarm Management:**
    *   Create, Read, Update, Delete alarms.
    *   Set Time (HH:MM), Days of the week (repeat), Enabled/Disabled toggle.
*   **Radio Station Management & Favorites:**
    *   **Explore & Listen:** Search, browse, and play stations using the Radio Browser API as a standalone player.
    *   **Favorites List:** Save and remove favorite stations.
    *   **Set as Alarm:** Seamlessly assign any explored or favorited station to an alarm, reusing this interface to reduce extraneous components.

### User Flow
1.  **Home Screen (Alarms):** User opens the app and sees a list of active and inactive alarms. An "Explore Radio" button is prominently available to navigate to the radio section.
2.  **Explore Radio:** A dedicated screen to search, play, and favorite radio stations. This single screen acts as both the general listening player and the selection screen when assigning a station to an alarm.
3.  **Add/Edit Alarm:** User taps "+" to add an alarm. They set the time and repeating days. Setting the station redirects to the "Explore Radio" screen, allowing for component reuse.
4.  **Alarm Trigger:** At the scheduled time, the app wakes the device, starts buffering and playing the selected live radio stream, and locks the screen into the "Wake Up" activity.
5.  **Dismissal Challenge:** A random, simple multiple-choice question (e.g., "What is 7 + 5?", "Capital of France?") is displayed with 4 choices.
6.  **Completion:** The user taps the correct answer and holds their finger down. An animation fills the button over 3 seconds. Once full, the alarm stops, and the user is returned to the Home Screen.

### Non-Functional Requirements
*   **Performance:** App size must be strictly minimized (< 10MB ideal). Radio stream must buffer within 2-3 seconds of alarm trigger (fallback regular tone if no internet).
*   **Reliability:** Must fire reliably from the background, bypassing Android Doze mode and manufacturer battery optimizations via exact alarms (`AlarmManager` / `WorkManager`).
*   **Architecture:** Pure Kotlin, using native Android UI (XML or Jetpack Compose, leaning towards Compose for easier animations).

### Success Criteria
*   Alarm triggers precisely on time (±10 seconds) even after device reboot or app being killed.
*   Radio stream plays successfully >95% of the time, with a built-in fallback ringtone if network is unavailable.
*   APK size remains incredibly small and performance is smooth on low-end devices.
