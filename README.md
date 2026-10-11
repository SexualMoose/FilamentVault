# FilamentVault

A fully offline Android app for cataloging 3D-printing filament spools and per-material print-setting
profiles.

**Status:** working, v2.1.2 (versionCode 3); no code changes since June 2026.

## Features
- Spool inventory with material, color (hex, HSV picker, or pick from photo), brand, modifier, diameter,
  quantity, notes and full print settings; spool photos from gallery or camera with a spool-shaped cropper.
- Built-in database of 287 print profiles (`defaults.json` ships 291 entries; 4 duplicate keys collapse on
  import) that auto-fill new spools; edit, override, or add your own.
- Color-wheel-sorted inventory with filters (material, modifier, brand, temperatures, diameter).
- JSON backup/restore to any location you choose (merge or replace); light/dark/system theme.
- No account, no network, no `INTERNET` permission — everything stays on the device.

## Quickstart
Requires JDK 17 to 23 (JDK 21 recommended, e.g. Android Studio's bundled JBR; the Gradle 8.11.1 wrapper fails
on JDK 24+), Android Studio Ladybug or newer, and Android SDK Platform 35. The app installs only on Android 15+
(`minSdk` 35).

Before running Gradle from a terminal, set `ANDROID_HOME` or create `local.properties` with
`sdk.dir=<path to your Android SDK>` (Android Studio creates this file when you open the project); otherwise
the build fails with "SDK location not found".

```sh
git clone https://github.com/SexualMoose/FilamentVault.git
cd FilamentVault
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties   # macOS default SDK path; adjust, or export ANDROID_HOME instead
chmod +x gradlew              # gradlew is committed without the executable bit (or use `sh gradlew`)
./gradlew :app:installDebug   # or :app:assembleDebug → app/build/outputs/apk/debug/
```

Release builds (`./gradlew :app:assembleRelease`) are unsigned; the repo contains no signing configuration.

## Documentation
Architecture, dependencies and licenses, configuration, known issues and security notes are in
[PROJECT-DOCUMENTATION.md](PROJECT-DOCUMENTATION.md). Bundled third-party files are listed in
[THIRD-PARTY.md](THIRD-PARTY.md).

## License
No license has been chosen yet: there is no `LICENSE` file, so all rights are reserved by default even though
the repository is public.

Filament brand names in the built-in database are trademarks of their respective owners; this project is not
affiliated with any of them.
