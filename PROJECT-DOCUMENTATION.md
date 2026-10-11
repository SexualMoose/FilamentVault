# FilamentVault — Project Documentation

A fully offline Android app for cataloging 3D-printing filament spools and per-material print-setting profiles.

_Last audited: 2026-10-11 against main @ 6b3a8ee._

## Overview / Purpose
FilamentVault is a local-first inventory manager for 3D-printer filament spools. Each entry records the
material type, color (hex + optional name, plus an optional photo of the spool), brand, modifier (called
"fill type" in code: Silk, Matte, Carbon Fiber, etc.), filament diameter (1.75 or 2.85 mm), quantity, and a
large set of print parameters (nozzle/bed temperature ranges, chamber temp, print-speed range, max volumetric
speed, fan-speed range + notes, retraction distance/speed, flow rate, layer-height range, density, drying
temp/time, moisture sensitivity, notes).

The app ships a built-in "defaults database" of 287 recommended print profiles
(`app/src/main/assets/defaults.json` ships 291 entries; 4 duplicate keys collapse on import) covering 16
material types, keyed by material and optionally brand and modifier. These auto-populate new inventory entries. Users can override built-in entries (the original is
preserved and can be restored) or add their own custom entries.

There is no account, no cloud sync, and no network access: the manifest declares no `INTERNET` permission.
All data lives in a local Room (SQLite) database, app-internal image files, and an optional user-exported
JSON backup file.

## Status
Working; dormant since June 2026. Evidence:
- `versionName = "2.1.2"`, `versionCode = 3`, `applicationId = "com.filamentvault"` in `app/build.gradle.kts`.
- Git history has 3 commits: `19a15fd` (2026-04-21, "Initial release v2.1.1"), `f9ccabd` (2026-06-25, bumps
  to 2.1.2 / vc3; the message also mentions moving to a different release signing key, but the diff only
  changes the version — no signing configuration is in the repo), and `6b3a8ee` (2026-06-27, docs). No code
  changes since 2026-06-25.
- Branches: only `main` (local and `origin`); no tags; no remote branch carries commits that are not on `main`.
- Five Room schema versions committed under `app/schemas/` (1.json–5.json); the database is at version 5
  with explicit migrations for 3→4 and 4→5.
- Feature-complete UI: inventory list, add/edit screen, filter sheet, filament database browser and editor,
  settings, JSON backup/restore, color picker (including pick-from-photo) and spool-photo cropper.
- No TODO/FIXME markers in source. No test sources and no CI workflows.

## Technical Requirements
- Language: Kotlin 2.1.0 (KSP 2.1.0-1.0.29). Java/Kotlin bytecode target 17
  (`sourceCompatibility`/`targetCompatibility` = 17, `jvmTarget = "17"`).
- Build: Gradle 8.11.1 (wrapper), Android Gradle Plugin 8.7.3, Kotlin DSL, version catalog in
  `gradle/libs.versions.toml`. Gradle 8.11.1/AGP 8.7.3 need JDK 17–23 (JDK 21 recommended, e.g. Android
  Studio's bundled JBR; the Gradle 8.11.1 wrapper fails on JDK 24+).
- Android SDK: `compileSdk` = `targetSdk` = `minSdk` = 35 (Android 15). Because `minSdk` is 35, the app only
  installs on Android 15+ devices/emulators.
- UI: Jetpack Compose (BOM 2024.12.01), Material 3.
- Hardware: a camera is optional (used to photograph spools; the gallery picker works without one).
- Accounts / keys: none to build a debug APK or run the app. Release signing uses a key kept outside the
  repository; no `signingConfigs` block is committed (see Build & Run).
- Network is needed only at build time: Google Maven, Maven Central and the Gradle Plugin Portal for
  plugins; Google Maven, Maven Central and JitPack for libraries; `services.gradle.org` for the Gradle
  wrapper distribution.

## Dependencies
All versions are declared in `gradle/libs.versions.toml`.

| Library | Version | License |
|---|---|---|
| AndroidX Core KTX (`androidx.core:core-ktx`) | 1.15.0 | Apache-2.0 |
| AndroidX Activity Compose | 1.9.3 | Apache-2.0 |
| Jetpack Compose BOM (ui, ui-graphics, ui-tooling-preview, material3, material-icons-extended; ui-tooling debug only) | 2024.12.01 | Apache-2.0 |
| AndroidX Lifecycle (runtime-compose, viewmodel-compose) | 2.8.7 | Apache-2.0 |
| AndroidX Navigation Compose | 2.8.5 | Apache-2.0 |
| AndroidX Room (runtime, ktx, compiler via KSP, Gradle plugin) | 2.6.1 | Apache-2.0 |
| Hilt / Dagger (`hilt-android`, `hilt-android-compiler`, Gradle plugin) | 2.53.1 | Apache-2.0 |
| AndroidX Hilt Navigation Compose | 1.2.0 | Apache-2.0 |
| Coil 3 (`io.coil-kt.coil3:coil-compose`) | 3.0.4 | Apache-2.0 |
| kotlinx-coroutines-android | 1.9.0 | Apache-2.0 |
| kotlinx-serialization-json | 1.7.3 | Apache-2.0 |
| skydoves colorpicker-compose (`com.github.skydoves:colorpicker-compose`, via JitPack) | 1.1.2 | Apache-2.0 |
| AndroidX DataStore Preferences | 1.1.1 | Apache-2.0 |
| AndroidX ExifInterface | 1.3.7 | Apache-2.0 |
| Build plugins: AGP, Kotlin Android/Compose/Serialization, KSP | see versions above | Apache-2.0 |
| Gradle Wrapper (bundled `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`) | 8.11.1 | Apache-2.0 |

No copyleft (GPL/LGPL/MPL/AGPL) or non-commercial dependencies are present.

## Setup Instructions
1. Install JDK 17 to 23 (JDK 21 recommended, e.g. Android Studio's bundled JBR; the Gradle 8.11.1 wrapper fails
   on JDK 24+) and Android Studio Ladybug or newer (compatible with AGP 8.7.3) with Android SDK Platform 35.
2. Clone the repo:
   `git clone https://github.com/SexualMoose/FilamentVault.git`
3. Create `local.properties` at the repo root pointing to your SDK (Android Studio generates it on first
   open): `sdk.dir=<path to your Android SDK>`. The file is git-ignored. Alternatively, export `ANDROID_HOME`
   pointing to the SDK. Without one of the two, terminal builds fail with "SDK location not found".
4. Open the project in Android Studio and let Gradle sync. Plugins resolve from Google Maven, Maven Central
   and the Gradle Plugin Portal; libraries from Google Maven, Maven Central and JitPack (all configured in
   `settings.gradle.kts`); the wrapper downloads Gradle 8.11.1 from `services.gradle.org`.
5. To build from a terminal, make the wrapper executable first. `gradlew` is committed without the executable
   bit, so `./gradlew` fails with "permission denied" on a fresh clone:
   `chmod +x gradlew` (or run every command below as `sh gradlew …`).

## Build & Run
Run `chmod +x gradlew` once after cloning (see Setup step 5), or use `sh gradlew` instead of `./gradlew`.
- Assemble a debug APK:
  `./gradlew :app:assembleDebug` → `app/build/outputs/apk/debug/`
- Build and install on a connected Android 15+ device or emulator:
  `./gradlew :app:installDebug`
- Release build (`isMinifyEnabled = true`, `isShrinkResources = true`, R8 with `app/proguard-rules.pro`):
  `./gradlew :app:assembleRelease`
  The build file has no signing configuration, so this produces an unsigned APK under
  `app/build/outputs/apk/release/`; sign it with your own key (e.g. `apksigner`) before installing. The
  owner's release builds are signed outside this repository.
- There are no tests to run (see Testing).

## Usage
The app has a bottom navigation bar with three tabs: **Inventory**, **Database**, **Settings**.

1. **Inventory** (home): your spools, always sorted in color-wheel order (grays last, then hue, saturation,
   lightness). Tap **+** to add a spool, tap a card to edit it, and swipe a card left to delete it (with
   confirmation; the edit screen has no delete action). The filter sheet filters by material type,
   fill type/modifier, brand (brands present in your inventory), nozzle temperature range, bed temperature
   range, and filament diameter.
2. **Add / edit a spool:** choose a material (and optionally brand and modifier) and the print settings
   auto-fill from the defaults lookup — your own overrides/custom entries first, then the built-in catalog,
   from most specific (material + brand + modifier) down to material only. Set the color by hex, an HSV
   picker, or by picking a pixel from the spool photo. Add a photo from the gallery (Android photo picker) or
   the camera, then crop it to a spool silhouette (pan, pinch-zoom, rotate); the original photo is kept so you
   can re-crop. Saving a spool that matches an existing one (same material, color, diameter, brand and
   modifier) offers to increase that entry's quantity instead ("Add Spool").
3. **Database:** browse and search (material, brand, or modifier) the 287 built-in profiles (`defaults.json`
   ships 291 entries; 4 duplicate keys collapse on import) plus your custom ones. Editing a built-in entry
   saves an override (the original is preserved and can be restored); you can also save as a new custom
   entry, duplicate, or delete custom entries. Note that the override's link to its built-in entry is lost on
   the next app start (see Known Issues).
4. **Settings:**
   - Theme: System / Dark / Light (default Dark).
   - Display: "Show thumbnails" (off by default) toggles a photo (or color square) on each inventory row.
   - Backup & Restore: **Export backup** writes `filamentvault_backup_<timestamp>.json` to a location you pick
     through the Storage Access Framework (device storage or any installed document provider, e.g. a cloud
     drive app). **Import backup** restores a file, either merging with or replacing current data. Backups
     contain inventory rows and your database customizations only — not the built-in catalog and not photo
     files (see Known Issues).
   - Database: **Reset all default edits** removes every override and custom entry (inventory unaffected).

## Architecture
Clean-architecture / MVVM, Hilt-injected, single-Activity Compose app (package `com.filamentvault`).
- Entry: `FilamentVaultApp` (`@HiltAndroidApp`) → `MainActivity` (`@AndroidEntryPoint`, edge-to-edge) applies
  the theme from `SettingsViewModel` and hosts `FilamentVaultNavGraph`.
- Navigation (`ui/navigation/NavGraph.kt`): a root `NavHost` (`home`, `filament_detail/new`,
  `filament_detail/{filamentId}`, `defaults_edit/{baseId}/{overrideId}`); `home` is a shell with a bottom bar
  and a nested `NavHost` for the `inventory`, `database`, and `settings_tab` tabs.
- Presentation (`ui/`): Compose screens + ViewModels per feature — `filamentlist` (card, empty state,
  filter sheet), `filamentdetail` (image section, color picker, image color picker, spool cropper, brand
  autocomplete, material/fill-type dropdowns, temperature and print-settings sections, notes), `defaultsdb`
  (list + editor), `settings`; shared `ui/common`; theming in `ui/theme` (OLED dark and light schemes).
- Domain (`domain/`): models (`Filament`, `MaterialType` — 16 materials, `FillType` — 18 modifiers,
  `FilterCriteria`) and use cases (`AddFilamentUseCase`, `DeleteFilamentUseCase`, `GetFilamentsUseCase`,
  `LookupDefaultsUseCase`).
- Data (`data/`): `FilamentRepository` / `FilamentRepositoryImpl`; Room `FilamentVaultDatabase` (version 5,
  file `filamentvault.db`) with tables `filaments`, `default_settings` (built-in catalog) and
  `default_overrides` (user overrides with `base_id`, or custom entries with no `base_id`); DAOs
  `FilamentDao`, `DefaultSettingsDao`, `DefaultOverrideDao`; `Converters` type converter. Schemas are exported
  to `app/schemas/`.
- DI (`di/`): `AppModule` (database + DAOs, migrations, defaults callback), `RepositoryModule`.
- Utilities (`util/`): `BackupUtil` (kotlinx-serialization JSON export/import through SAF URIs),
  `DefaultsPopulator` (Room `onOpen` callback that seeds `default_settings` from `assets/defaults.json` when
  the table is empty or its row count differs from the asset's entry count; with the shipped asset that is
  every open, see Known Issues), `ImageStorageUtil` (copies spool images into
  app-internal `filament_images/`; camera captures go to `cache/camera_cache/` through the `FileProvider`),
  `ColorWheelSort`.
- Preferences: Jetpack DataStore file `settings` (keys `theme_mode`, `show_thumbnails`).
- Data flow: Compose UI → ViewModel → UseCase/Repository → Room DAO ↔ SQLite. Images live in internal
  storage; the catalog is seeded from a bundled asset.

## Integrations & Interconnects
- **Network: none.** No HTTP/socket code, no Firebase/analytics/cloud SDK, and no `INTERNET` permission in
  the app manifest.
- **Android platform only:** Storage Access Framework (`CreateDocument` / `OpenDocument`) for the backup file
  — any document provider the user has installed can be the destination; the Android photo picker
  (`PickVisualMedia`) for gallery images; the system camera app via `ActivityResultContracts.TakePicture`
  (no CameraX); and a non-exported `FileProvider` (`${applicationId}.fileprovider`) that hands the camera a
  cache file URI.
- **Other repos and services:** the app has no client for any external server or device-management service,
  and nothing in this repo depends on one. A separate iOS port lives in the public FilamentVault-iOS repo; the
  two share no code and do not sync data. The iOS repo bundles a byte-identical copy of `defaults.json`
  (checked 2026-10-11), so catalog changes must be mirrored there. Backup files cannot be imported across
  platforms: Android writes timestamps as epoch milliseconds and iOS writes ISO-8601 date strings.
- **Reference data only:** `assets/defaults.json` names real filament manufacturers (about 47, e.g. Bambu Lab,
  Prusament, Polymaker, eSUN, Hatchbox, Overture) as brand/profile data — no API, integration, or affiliation
  with any of them.

## Configuration & Secrets
- No secrets are needed to build a debug APK or run the app. The code reads no environment variables,
  `BuildConfig` fields, or `local.properties` keys beyond the standard `sdk.dir`.
- `local.properties` (SDK path) is git-ignored and must be created locally, or set `ANDROID_HOME` instead.
- Release signing: the signing keystore and its passwords are kept outside the repository and are not
  referenced by the build files; never commit them (`.gitignore` excludes `*.jks` / `*.keystore`).
- Runtime configuration is user-set in the app (DataStore `settings`: theme mode, show thumbnails).

## Testing
No automated tests exist: there is no `src/test` or `src/androidTest`, and no JUnit/Espresso/androidx.test
dependencies (`testInstrumentationRunner` is set in `defaultConfig` but unused). Verification is manual.
The exported Room schemas under `app/schemas/` would allow Room migration tests, but none are wired up.

## Known Issues / TODO
- No test coverage — notably for backup import/export, the defaults lookup cascade, and Room migrations.
- No CI workflows (`.github/` does not exist).
- `minSdk = 35` (Android 15) excludes most devices; it reflects a personal target rather than a
  public-distribution choice.
- The Settings → About text is hard-coded as "FilamentVault v2.1.1" while the build is `versionName` 2.1.2.
- Backups do not include photos: the export omits the cropped thumbnail path and stores only an app-internal
  path for the original photo, so restored entries come back without a thumbnail (and photos never transfer to
  another device).
- Only the 3→4 and 4→5 migrations exist and the database is built with `fallbackToDestructiveMigration()`, so
  an upgrade from schema 1 or 2 — or any future schema bump shipped without a migration — wipes local data.
- `defaults.json` contains 4 duplicate fully-specified keys that collapse under the unique index, so
  `default_settings` holds 287 rows and `DefaultsPopulator`'s count check (287 != 291) re-seeds the table on
  every app start with new AUTOINCREMENT ids. Overrides linked by `base_id` therefore detach after every
  restart (edited built-ins show as unmodified and the override disappears from the Database tab; backups
  export it as a custom entry). Auto-fill still applies overrides because lookup matches by
  material/brand/modifier.
- `FilterCriteria` supports chamber-temperature, moisture-sensitivity and text-search filters, but the filter
  sheet exposes none of them and the inventory screen has no search field.
- UI wording is inconsistent: the modifier field is labelled "Fill Type" in the filter sheet.
- `defaults.json` has a casing duplicate brand ("ColorFabb" / "Colorfabb").
- `defaults.json` has 22 duplicate material/brand/modifier keys (4 collapse on import; the other 18 involve a
  missing brand or modifier and stay as separate rows, and lookups use the last one in the file) and 12
  entries whose modifier string is not one of the 18 selectable modifiers (for example "High Speed" instead
  of "High Speed (HS)"), so auto-fill can never pick them by modifier.
- `gradlew` is committed without the executable bit (mode 100644), so `./gradlew` needs `chmod +x gradlew`
  first on a fresh clone.
- No `LICENSE` file yet (see Third-party & Licensing).

## Third-party & Licensing
- **Project license:** no license has been chosen yet. There is no `LICENSE` file, so although the repository
  is public, all rights are reserved by default and no open-source grant is given.
- All third-party libraries are Apache-2.0 (permissive). No GPL/LGPL/MPL/AGPL or non-commercial dependencies.
- Bundled third-party files: only the Gradle Wrapper (Apache-2.0) — listed in `THIRD-PARTY.md`. No vendored
  or copied source trees, no foreign license headers in app source, no bundled fonts, models or third-party art
  (launcher icons are original vector drawables). Not a fork — history starts with this project's own commit.
- Catalog data: `defaults.json` print-setting values are factual reference data; their source is not
  documented in the repo.
- Trademark note (low risk): manufacturer brand names appear as reference values in `defaults.json` and in
  brand autocomplete. This is nominative/descriptive use, not branding of the app; the app name, package
  (`com.filamentvault`) and icon do not imitate any vendor. If distributed publicly, add a short
  "trademarks belong to their owners; not affiliated" line.

## Security notes
- **Trust boundaries:** the app is offline. Data stays in the app sandbox (Room database, internal image
  files, DataStore) except when the user explicitly exports a backup, which is plain, unencrypted JSON written
  wherever the user chooses. Imported backup files are untrusted input parsed with kotlinx-serialization.
  There is no WebView, JavaScript bridge, or dynamic code loading, and the only raw SQL is constant
  schema-migration DDL.
- **Auth model:** none — no accounts, logins, or tokens.
- **Secrets:** none in the code or git history; the release signing key lives outside the repository.
- **Permissions / components:** the source manifest requests only `CAMERA` (at runtime, when the user taps
  Camera) and exports only `MainActivity`; the `FileProvider` is not exported and is scoped to two app paths.
  The merged manifest adds library entries: androidx.core's signature-level
  `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` and profileinstaller's `ProfileInstallReceiver` (exported but
  `DUMP`-protected); debug builds also export Compose's `PreviewActivity`. Neither the debug nor the release
  merged manifest requests `INTERNET`.
- **Known risk categories (high level):** app data is eligible for Android backup (`allowBackup="true"`), and
  exported backups are unencrypted. The inventory data is not sensitive.
- **Overall:** the October 2026 review found nothing above low severity; the low-severity item is the
  `allowBackup` setting above.
