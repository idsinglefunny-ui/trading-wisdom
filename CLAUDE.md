# TradeYourPlan (交易智慧) - Project Guide

## Build Environment

- **JDK**: 17 (path: `/home/mnyagent/.local/java/jdk-17.0.12`)
- **Kotlin**: 1.9.20, JVM target: 17
- **Android SDK**: compileSdk 34, targetSdk 34, minSdk 24
- **Gradle**: 8.5
- **R8/Minification**: DISABLED (`isMinifyEnabled = false`) — do not enable, Gson TypeToken breaks under R8

## Build Command

```bash
export JAVA_HOME=/home/mnyagent/.local/java/jdk-17.0.12 && export PATH="$JAVA_HOME/bin:$PATH" && ./gradlew assembleRelease --no-daemon
```

Release APK output: `app/build/outputs/apk/release/app-release.apk`

## Signing

- Keystore: `app/typ-release.jks`
- Store password: `tradeyourplan`
- Key alias: `typ`
- Key password: `tradeyourplan`

## Tech Stack

- Jetpack Compose + Material3
- MVVM + Repository pattern
- Hilt DI
- Room database (version 3, migrations 1→2→3)
- Gson (for loading system quotes JSON)
- DataStore (settings persistence)
- AlarmManager (scheduled notifications)
- Navigation: single-screen with bottom tabs (no separate screen routes)
