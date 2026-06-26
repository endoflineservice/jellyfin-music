# Play Store Readiness

This file tracks the repo-side pieces needed for a Play Store submission. Play Console fields still need to be completed in the console by the publisher.

## Release Signing

Release builds no longer use the debug signing key. To create a Play-ready signed APK or App Bundle, create a local `keystore.properties` file from `keystore.properties.example` or provide the same values as environment variables:

```text
JELLYFIN_MUSIC_UPLOAD_STORE_FILE=C:/path/to/jellyfin-music-upload.jks
JELLYFIN_MUSIC_UPLOAD_STORE_PASSWORD=...
JELLYFIN_MUSIC_UPLOAD_KEY_ALIAS=...
JELLYFIN_MUSIC_UPLOAD_KEY_PASSWORD=...
```

`keystore.properties`, `.jks`, and `.keystore` files are ignored by git.

Build the Play upload bundle with:

```bash
./gradlew verifyPlayReleaseSigning bundleRelease
```

The App Bundle output is:

```text
app/build/outputs/bundle/release/app-release.aab
```

If signing is not configured, Gradle can still produce local release outputs for inspection, but `verifyPlayReleaseSigning` will fail and the output is not Play-upload ready.

## Privacy Policy

Use `docs/privacy-policy.md` as the starting privacy policy. Before publishing, replace the contact section with the real support email or public repository URL used in the Play Store listing.

## Data Safety Form Draft

Recommended Play Console answers based on the current app:

- Analytics: No.
- Advertising: No.
- Data sold: No.
- Data shared with advertising/analytics SDKs: No.
- Account creation inside app: No. The app signs into a user-provided Jellyfin server.
- User-provided server data: The app transmits login and media requests to the Jellyfin server URL entered by the user.
- Data encrypted in transit: Yes only when the user enters an `https://` Jellyfin server URL. The app currently allows `http://` servers for local/self-hosted setups, so do not overstate this in the Play form.
- Deletion request path: Users can sign out in the app and clear app storage from Android settings. Jellyfin account deletion is handled on the user's Jellyfin server.
- Google Play purchase data: The optional one-time support purchase is processed by Google Play Billing.

Data types the app uses locally:

- App activity: playback state, queue, favorites, and settings.
- App info and performance: no crash/analytics SDK is currently included.
- Personal info: Jellyfin username/user ID and server URL are stored locally after sign-in.
- Audio/music library metadata: track titles, artists, albums, genres, durations, artwork URLs, and cached artwork from the user's Jellyfin server.

## Foreground Service Declaration Draft

Foreground service type: `mediaPlayback`

Purpose:

Jellyfin Music uses a media playback foreground service only while music is playing or buffering so audio can continue when the app is in the background, the screen is off, Android Auto is connected, or the user controls playback from a media notification.

User-visible behavior:

The service shows a persistent media notification with the current track and playback controls. The notification lets the user pause, resume, skip, and stop playback.

Trigger:

The service starts after a user begins playback by tapping a song, pressing play, using the widget, using the notification, or using Android Auto media controls.

Stop condition:

The service stops foreground playback when the user stops playback, playback ends with no next track, or the active playback session is released.

Suggested review demo:

1. Open the app and connect to a Jellyfin server.
2. Start a song.
3. Press Home or lock the screen.
4. Show the active media notification and playback continuing.
5. Pause/stop playback from the notification and show the service ending foreground playback.

## Android Auto Review Notes

The app declares a `MediaBrowserService` for Android Auto and publishes media metadata, queue, playback state, and custom actions. Before public release, verify with Android Auto Desktop Head Unit or a real vehicle:

- Now playing album art appears for local bitmap metadata and content URI metadata.
- Shuffle state visibly changes between off and on.
- Browse pages expose useful Tracks, Albums, Artists, Favorites, and Shuffle All entry points.
- Playback continues across screen-off next-track transitions.

## Stability Gate

Before moving from closed testing to production, complete at least one playback soak test:

- Start shuffled playback from Library.
- Lock the screen.
- Let at least 10 tracks advance naturally.
- Use notification pause/play/next.
- Connect Android Auto and confirm the existing shuffled queue is preserved.
- Check there is only one active media notification.
