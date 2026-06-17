# Jellyfin Music

A small native Android prototype for a clean Jellyfin-focused music player.

## Current Prototype

- Kotlin and Jetpack Compose Android app.
- Material You themed UI with dynamic color on Android 12+.
- Jellyfin server login with saved session.
- Real song library loading from the Jellyfin API.
- Search plus songs, albums, and artists library views.
- Full-screen player with large rounded art, animated visualizer bars, wavy seek, shuffle, repeat, auto-next, play, pause, and replay.
- Home-screen widgets: a vinyl-inspired 4x2 player card and a compact 2x1 mini widget.
- App label and package identity set to `Jellyfin Music`.

## Study Plan

1. Study Finamp for Jellyfin auth, library sync, offline caching, and playback edge cases.
2. Review Gelli for older Android-native Jellyfin music patterns and UX tradeoffs.
3. Use the Jellyfin Kotlin SDK for server auth, user sessions, item browsing, and stream URLs.
4. Keep playback logic separated from UI so the app can swap mock data for real Jellyfin data cleanly.
5. Build the first real milestone around server login, album/song browsing, and basic playback.

## Local Build

```bash
./gradlew assembleDebug
```
