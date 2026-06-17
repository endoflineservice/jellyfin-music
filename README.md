# Jellyfin Music

A small native Android prototype for a clean Jellyfin-focused music player.

## Current Prototype

- Kotlin and Jetpack Compose Android app.
- Simple library screen with mocked tracks.
- Rounded, quiet UI aimed at fast scanning.
- Animated wiggly progress bar in the mini player.
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
