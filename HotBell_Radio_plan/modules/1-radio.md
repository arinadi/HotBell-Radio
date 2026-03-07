# Module 1: Radio Integration

* **Estimated Complexity:** M (~10 files)
* **Estimated Files:** ~10
* **Key Risks:** ExoPlayer streaming reliability over cellular and handling radio-browser DNS rotation.

## Requirements
* Integrate Radio Browser API (Retrofit) for searching and fetching stations.
* Implement a background Media3 `MediaSessionService` / ExoPlayer instance that can stream audio when requested.
* Implement a `RadioRepository` to provide radio data and manage playback state.

## UI Structure
* No UI screens. Exposes `StateFlow` of playback state (Playing, Buffering, Error) to be consumed by other modules.

## Data & API
* **API Endpoints (Retrofit):**
    * `GET /json/stations/search`
    * `POST /json/url/{stationuuid}` (Click counter and resolved URL fetcher)
* **Models:** `StationNetworkModel` (matching `api_radio-browser.md`).
* **Repository:** `RadioRepository.kt` combining Retrofit API calls.

## Technical Implementation
* Initialize Retrofit with base URL `https://all.api.radio-browser.info/`.
* Implement a Media3 `MediaLibraryService` or standard `MediaSessionService` containing an `ExoPlayer` instance.
* Player must handle transient network loss and automatically retry buffering.
* Must declare `<uses-permission android:name="android.permission.INTERNET" />` and `<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />`.
* `android:usesCleartextTraffic="true"` in Manifest for non-HTTPS radio streams.

## Testing
* **Check 1:** Calling `RadioRepository.searchStations("jazz")` returns a non-empty list of stations.
* **Check 2:** Calling `RadioRepository.play(url_resolved)` successfully buffers and starts playback (verifiable via logcat or StateFlow).
* **Check 3:** Passing an invalid URL to ExoPlayer transitions the state to an Error state within 5 seconds.
