# DeskMint Dashboard

Offline Android dashboard/launcher built for the **Lenovo A3000H** (Android 4.2–4.4,
API 17–19). Designed to sit permanently on a desk as a clock/media/productivity panel.

## What's included (working source, not a compiled APK)

This is a full Android Studio project (Java, Gradle). It was written and organized
in this sandbox, which has no internet access and no Android SDK/emulator installed,
so **it has not been compiled or run** — treat it as a strong, working starting point
that still needs a build pass in Android Studio (or `gradlew assembleDebug` on a
machine with the Android SDK and network access) to catch any typos or API-level
quirks before it goes on the tablet.

### Implemented
- **MainActivity** — swipeable ViewPager dashboard with 6 panels + tab strip.
- **Home panel** — live digital clock, date, offline-cached weather (Open-Meteo, no key).
- **Tasks panel** — to-do list backed by local SQLite (`DbHelper`), add/complete/delete.
- **Apps panel** — alarm list (add/delete) + link into the full app drawer.
- **App Drawer** — searchable list of installed apps; search bar doubles as a command
  bar (`wifi`, `bt`, `calc:2+2`).
- **Media panel** — links to Music Player (scans `MediaStore.Audio`, background
  `MusicService` with notification controls, play/pause/next/prev) and Video Gallery
  (scans `MediaStore.Video`, resumes last position per file).
- **Games panel** — Snake (SurfaceView/Canvas), Tic-Tac-Toe (vs simple AI), Memory
  Match — each with a local high score in SharedPreferences.
- **Settings panel** — Wi-Fi toggle, Bluetooth toggle, brightness slider, volume
  slider, flashlight toggle.
- **Alarms** — `AlarmManager` + full-screen ring activity with snooze/dismiss,
  gradual volume ramp, vibrate; `BootReceiver` re-schedules alarms after reboot.

### Still to do before shipping
- Add real launcher icons (`res/mipmap`) — a placeholder reference exists in the
  manifest (`@drawable/ic_launcher`) but no image file is included.
- Calendar month-grid view (events table + DAO already exist in `DbHelper`; only the
  month-grid UI is left to build — `getEventsForDay()` is ready to use).
- Wire up an "Add Event" dialog similar to `TasksFragment`'s add-todo dialog.
- Notes widget, file manager, photo-slideshow screensaver, backup/restore-to-JSON,
  and the equalizer were described in the spec but not yet coded — the architecture
  (SQLite for structured data, SharedPreferences for simple settings, one Activity or
  Fragment per feature) makes them straightforward to bolt on the same way the
  existing features were built.
- Test thoroughly on the actual Lenovo A3000H — screen size, available codecs, and
  Camera API flash support vary by device even within the same Android version.

## Building it

1. Open the `DeskMint/` folder in Android Studio (or import as a Gradle project).
2. Let Gradle sync (it will need `com.android.tools.build:gradle:3.5.4` and the
   Android 19 support libraries — see `app/build.gradle`).
3. Add launcher icons to `app/src/main/res/mipmap-*`.
4. Connect the tablet via USB (enable "Unknown sources" / USB debugging on it) and
   run, or build a signed APK and sideload it.
5. To make it the tablet's home screen: Settings > Apps > Default Apps > Home app.

## Project layout

```
app/src/main/java/com/deskmint/dashboard/
  MainActivity.java
  fragments/      HomeFragment, TasksFragment, MediaFragment, AppsFragment, GamesFragment, SettingsFragment
  db/             DbHelper.java (SQLite: todo, event, alarm, note tables)
  alarm/          AlarmReceiver, AlarmRingActivity, AddAlarmActivity
  music/          MusicService, MusicPlayerActivity
  video/          VideoPlayerActivity, VideoGalleryActivity
  weather/        WeatherHelper (offline-cached)
  apps/           AppDrawerActivity (search + command bar)
  games/          SnakeActivity, TicTacToeActivity, MemoryMatchActivity
  util/           BootReceiver
```
