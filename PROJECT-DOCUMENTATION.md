# FilamentVault

A fully offline Android app for cataloging 3D-printing filament inventory and per-material print-setting profiles.

## Overview / Purpose
FilamentVault is a local-first inventory manager for 3D-printer filament spools. Each entry records the
material type, color (with hex + optional name and a photo of the spool), brand, fill type, quantity, and a
large set of print parameters (nozzle/bed/chamber temps, print speed, volumetric speed, fan speed, retraction,
flow rate, layer height, density, drying temp/time, moisture sensitivity, notes). The app ships a curated
"defaults database" of recommended print settings keyed by material (and optionally brand/fill type) that
auto-populates new entries; users can override these defaults and customize the database. There is no account,
no cloud sync, and no network access of any kind — all data lives in a local Room (SQLite) database and a
user-controlled backup file.

## Status
Working / released. Evidence:
- `versionName = "2.1.2"`, `versionCode = 3` in `app/build.gradle.kts`.
- Git history shows a tagged-style "Initial release v2.1.1" commit followed by a version bump.
- Five Room schema versions committed under `app/schemas/...` (1.json–5.json) with real `Migration` code in
  `FilamentVaultDatabase.kt`, indicating an evolving, shipped data model.
- Feature-complete UI: list, detail/editor, filtering, settings, defaults DB editor, backup/restore, color
  picker (including pick-from-image and spool cropper).
- No TODO/FIXME/WIP markers found in source. No test suite exists (no `src/test` or `src/androidTest`).

## Technical Requirements
- Language: Kotlin 2.1.0; Java 17 toolchain (`sourceCompatibility`/`targetCompatibility` = 17).
- Build: Gradle (Kotlin DSL) with Android Gradle Plugin 8.7.3; KSP 2.1.0-1.0.29.
- Android SDK: `compileSdk` / `targetSdk` / `minSdk` all = 35 (Android 15). minSdk 35 means it only installs on
  Android 15+ devices.
- UI: Jetpack Compose (BOM 2024.12.01), Material 3.
- Hardware: a device/emulator with a camera is useful (CAMERA permission) for photographing spools, but not
  strictly required.
- Accounts / keys: NONE for running the app. Release signing uses an external "fleet release key" (no
  `signingConfigs` block is committed; signing is supplied at build/CI time via `local.properties` /
  environment, neither of which is in the repo).

## Dependencies (key libraries + licenses)
All dependencies are declared in `gradle/libs.versions.toml`. Licenses:
- AndroidX Core KTX, Activity Compose, Lifecycle, Navigation Compose, Room, DataStore, ExifInterface — Apache-2.0
- Jetpack Compose (UI, Material 3, material-icons-extended) — Apache-2.0
- Hilt / Dagger (`com.google.dagger`) — Apache-2.0
- Coil 3 (`io.coil-kt.coil3:coil-compose`) — Apache-2.0
- kotlinx-coroutines, kotlinx-serialization-json — Apache-2.0
- skydoves colorpicker-compose (`com.github.skydoves:colorpicker-compose`, via JitPack) — Apache-2.0
No copyleft (GPL/AGPL/LGPL) or non-commercial dependencies are present.

## Setup Instructions
1. Install JDK 17 and Android Studio (Ladybug or newer, AGP 8.7.3 compatible) with Android SDK Platform 35.
2. Clone the repo:
   `gh repo clone SexualMoose/FilamentVault`
3. Create a `local.properties` at the repo root pointing to your SDK (Android Studio generates this on first
   open), e.g. `sdk.dir=/Users/you/Library/Android/sdk`.
4. Open the project in Android Studio and let Gradle sync (it resolves dependencies from Google, Maven Central,
   and JitPack as configured in `settings.gradle.kts`).

## Build & Run
- Debug build/install on a connected Android 15 device or emulator:
  `./gradlew :app:installDebug`
- Assemble a debug APK:
  `./gradlew :app:assembleDebug`  → `app/build/outputs/apk/debug/`
- Release build (`isMinifyEnabled = true`, `isShrinkResources = true`) requires signing config NOT present in
  the repo (the "fleet release key"); supply it via `local.properties`/environment, then:
  `./gradlew :app:assembleRelease`
- No tests to run (no test sources).

## Usage
1. Launch the app; the filament list (with empty-state) is the home screen.
2. Tap add to create a filament: choose material type → print settings auto-fill from the bundled defaults
   database; adjust color (hex, named, pick-from-photo, or spool-crop), brand, quantity, and any print
   parameters; save.
3. Filter/sort the list via the filter sheet (by material, brand, color wheel ordering, etc.).
4. Edit the defaults database (Defaults editor/list screens) to tune recommended settings per material/brand.
5. Settings: switch theme mode (light/dark/system) and export/import a full backup. Export writes a JSON
   bundle to a location you pick via the Storage Access Framework (local storage, Downloads, Google Drive,
   OneDrive, etc.); import restores it.

## Architecture
Clean-architecture / MVVM, Hilt-injected, single-Activity Compose app.
- Entry: `FilamentVaultApp` (`@HiltAndroidApp`) → `MainActivity` (`@AndroidEntryPoint`) sets Compose content and
  hosts `FilamentVaultNavGraph`.
- Presentation (`ui/`): Compose screens + ViewModels per feature — `filamentlist`, `filamentdetail` (+ many
  components: color picker, image color picker, spool cropper, temperature/print-settings sections),
  `defaultsdb` (list + editor), `settings`; shared `ui/common`, theming under `ui/theme`, nav in
  `ui/navigation/NavGraph.kt`.
- Domain (`domain/`): models (`Filament`, `MaterialType`, `FillType`, `FilterCriteria`) and use cases
  (`AddFilamentUseCase`, `DeleteFilamentUseCase`, `GetFilamentsUseCase`, `LookupDefaultsUseCase`).
- Data (`data/`): `FilamentRepository`/`...Impl`; Room `FilamentVaultDatabase` (v5) with DAOs
  (`FilamentDao`, `DefaultSettingsDao`, `DefaultOverrideDao`), entities, and a `Converters` type converter.
  Schemas exported to `app/schemas/`.
- DI (`di/`): `AppModule`, `RepositoryModule`.
- Utilities (`util/`): `BackupUtil` (JSON export/import via SAF URIs), `DefaultsPopulator` (seeds the defaults
  DB from `assets/defaults.json` on first run / migration callback), `ImageStorageUtil` (saves spool images to
  app-internal `filament_images/`, shares via FileProvider), `ColorWheelSort`.
- Data flow: Compose UI → ViewModel → UseCase → Repository → Room DAO ↔ SQLite. Images stored on internal
  storage; defaults seeded from a bundled asset.

## Integrations & Interconnects
- NONE in the networking sense. The app makes no HTTP/socket calls, has no Firebase/analytics/cloud SDK, and
  requests no INTERNET permission.
- Android platform integrations only: Storage Access Framework (backup file location, incl. cloud providers the
  user has installed like Google Drive/OneDrive — the app just hands them a URI), CameraX/camera intent for
  spool photos, and a `FileProvider` (`${applicationId}.fileprovider`) for sharing internal images.
- Reference data only: `assets/defaults.json` names real filament manufacturers (Bambu Lab, Prusament,
  Polymaker, Hatchbox, eSUN, Overture, Creality, Elegoo, Anycubic, Sunlu, Inland, Fillamentum, Proto-pasta) as
  brand/profile data — no API or affiliation with any of them.

## Configuration & Secrets
- No secrets are needed to build a debug build or run the app.
- Release signing: an external "fleet release key" (per commit `f9ccabd`). It is NOT in the repo. Provide
  keystore path/passwords via `local.properties` or CI environment variables; never commit the keystore
  (`.gitignore` already excludes `*.jks`/`*.keystore`).
- `local.properties` (SDK path) is git-ignored and must be created locally.

## Testing
No automated tests exist (no `src/test` or `src/androidTest`, no JUnit/Espresso dependencies). Verification is
manual. Room schema JSONs under `app/schemas/` enable Room's migration testing but no such tests are wired up.

## Known Issues / TODO
- No test coverage at all — a notable gap for the backup import/export and Room migration paths.
- No README/LICENSE/CONTRIBUTING files in the repo (this doc is the only project documentation).
- `minSdk = 35` (Android 15) is unusually high and will exclude the vast majority of devices; likely a personal
  target rather than a public-distribution choice.
- No CI config committed despite the "fleet release key" workflow implied by commit history.

## Third-party & Licensing notes
- No `LICENSE` file → the project is "all rights reserved" by default; there is no stated open-source grant.
- All third-party libraries are Apache-2.0 (permissive, compatible). No GPL/AGPL/LGPL or non-commercial deps.
- No vendored or copied source files; no foreign copyright headers ("based on" hit was an ordinary code
  comment in `BrandAutocompleteField.kt`). Not a fork — history starts with this project's own "Initial
  release" commit.
- Trademark note (low risk): manufacturer brand names appear as reference data values in
  `app/src/main/assets/defaults.json` and in brand autocomplete. This is nominative/descriptive use (naming
  the real brands a profile applies to), not branding of the app itself. The app name/package
  (`com.filamentvault`) and icon do not impersonate any vendor. If ever published, a short "trademarks belong
  to their owners / not affiliated" disclaimer is advisable.

## Security notes
- No hardcoded secrets, API keys, tokens, or credentials anywhere in the working tree or git history.
- No committed keystores, `.env`, `google-services.json`, or service-account files (history scanned).
- No network code, no WebView, no JS bridge, no dynamic code execution.
- `execSQL` usages are static Room migration DDL (constant strings), not user-controlled — no SQL injection.
- Minimal permissions: only CAMERA. The single exported component is the launcher `MainActivity` (required);
  the `FileProvider` is `exported="false"` with scoped paths.
- `android:allowBackup="true"` means user data is included in Android cloud/adb backups; acceptable for a local
  inventory app but worth a conscious decision if the data is considered sensitive.
- Overall: clean. No findings above informational severity.
