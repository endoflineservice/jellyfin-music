# Contributing

Jellyfin Music is a FOSS Android project. Contributions are welcome, especially around Jellyfin playback reliability, library browsing, offline music workflows, accessibility, and clean Material You design.

## Development Setup

1. Install Android Studio or the Android command-line tools.
2. Use JDK 17.
3. Clone the repo.
4. Build the debug APK:

```bash
./gradlew assembleDebug
```

## Pull Requests

- Keep changes focused and easy to review.
- Match the existing Kotlin and Jetpack Compose style.
- Avoid committing generated build output, device dumps, APKs, local properties, or heap dumps.
- Include screenshots for visible UI changes when practical.
- Run `./gradlew assembleDebug` before opening a pull request.

## Design Direction

- Keep the UI simple, rounded, calm, and music-first.
- Prefer real controls over decorative text labels for playback actions.
- Material You and Android accessibility should guide color, scale, and touch targets.
- Jellyfin server credentials and tokens must stay local to the user device.

## Licensing

By contributing, you agree that your contribution is licensed under the MIT License used by this project.
