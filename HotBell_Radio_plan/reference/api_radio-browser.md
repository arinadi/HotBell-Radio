# Radio Browser API Reference

## General
* **Base URL:** `https://all.api.radio-browser.info/` (Round-robin DNS to available servers, e.g., `de1.api.radio-browser.info`).
* **Format:** Supports JSON, XML, M3U, PLS, etc. For this app, we strictly use `json`.
* **User-Agent Required:** Must send a descriptive User-Agent header, e.g., `HotBell_Radio/1.0`.

## Key Endpoints

### 1. Advanced Station Search
* **URL:** `GET /json/stations/search`
* **Query Parameters (Optional):**
    * `name`: Name of the station.
    * `countrycode`: ISO 3166-1 alpha-2 (e.g., `US`, `ID`).
    * `tag`: Genre or tag (e.g., `jazz`, `news`).
    * `limit`: Number of results (e.g., `50`).
    * `order`: Field to order by (e.g., `clickcount`, `votes`).
    * `reverse`: `true` or `false`.
* **Use Case:** "Explore Radio" search functionality.

### 2. Top Stations (By Clicks)
* **URL:** `GET /json/stations/topclick/{limit}`
* **Example:** `/json/stations/topclick/20`
* **Use Case:** Default view for the "Explore Radio" screen before a user types a search.

### 3. Get Station by UUID
* **URL:** `GET /json/stations/byuuid/{uuid}`
* **Use Case:** Fetching the latest stream URL for a saved alarm just before it rings (to ensure it hasn't changed).

### 4. Count Station Click
* **URL:** `POST /json/url/{stationuuid}`
* **Description:** Provides the actual stream URL and registers a click on the API. Recommended to call this before starting playback to support the radio-browser project.
* **Returns:** `{ "ok": true, "message": "ok", "networkId": "...", "name": "...", "url": "..." }`

## Data Structure (Station Object)
A simplified representation of what the JSON returns for each station:
```json
{
  "stationuuid": "96185ca8-0601-11e8-ae97-52543be04c81",
  "name": "Classic Rock Florida",
  "url": "http://198.58.98.83:8258/stream",
  "url_resolved": "http://198.58.98.83:8258/stream",
  "homepage": "http://www.classicrockflorida.com/",
  "favicon": "https://...",
  "tags": "classic rock,rock",
  "countrycode": "US",
  "votes": 1203,
  "codec": "MP3",
  "bitrate": 128
}
```

## Gotchas & Usage Rules
1. **Always use `url_resolved`** for playing audio, as `url` might be a playlist file (m3u/pls) that needs parsing, while `url_resolved` is usually the direct stream.
2. **Timeouts:** Radio streams might be offline. Implement a strict connection timeout (e.g., 5 seconds). If it fails, fallback to the default alarm tone immediately.
3. **HTTP vs HTTPS:** The API returns both HTTP and HTTPS streams. The Android app MUST have `android:usesCleartextTraffic="true"` enabled in the Manifest to play HTTP streams.
