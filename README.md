# Mafia Game

Android TV / Mobile hosted Mafia game with QR code joining.

## Structure
- `android/app` - Mobile & Tablet app (player client)
- `android/tv` - Android TV app (game host)
- `web/` - Web client for QR code participants (no app install needed)
- `functions/` - Firebase Cloud Functions (game logic)

## Tech Stack
- Android (Kotlin) + Jetpack Compose
- Firebase Realtime Database (real-time sync)
- Firebase Hosting (web QR join page)
- Firebase Cloud Functions (game logic)
