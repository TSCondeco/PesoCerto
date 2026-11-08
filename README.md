# Peso Certo

An offline-first Android weight tracker built with Jetpack Compose. Log your weight in seconds, see your trend, and stay on track towards your goal. All data stays on your device.

## Features

- **Quick logging**: bottom sheet with keypad and stepper, optional body fat and note, undo on delete.
- **Dashboard**: current weight, change, BMI, and goal progress with a projected completion date.
- **Interactive chart**: touch or drag to inspect any entry (date, weight, change, body fat). Time ranges from 1W to ALL.
- **Share**: share the Trends summary together with an image of the chart.
- **Adaptive streaks**: cadence is detected from how often you weigh in, so you get day, week or month streaks.
- **Ledger**: full history with CSV export.
- **Reminders**: scheduled daily notifications that survive reboots.
- **Widgets and quick add**: home-screen weight and chart widgets, plus a Quick Settings tile.
- **Settings**: height, goal weight, sex, date of birth, metric/imperial units, light/dark/system theme and in-app language.
- **Localization**: English, Portuguese, Spanish and French.
- **Accessibility**: content descriptions, plural-aware labels and large touch targets.

## Tech stack

| Area | Library |
|------|---------|
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| Persistence | Room, DataStore |
| DI | Koin |
| Language | Kotlin 2.0, Coroutines/Flow |
| Build | Gradle (Kotlin DSL), version catalog |

Min SDK 31, target/compile SDK 36, package `pt.condeco.peso`.

## Project structure

```
app/src/main/java/pt/condeco/peso/
├── data/            Room entities, DAO, DataStore preferences, repository
├── domain/          Pure Kotlin logic: units, BMI, stats, streaks, CSV
├── notifications/   Reminder scheduling and receivers
├── widget/          Home-screen widgets and Quick Settings tile
├── di/              Koin modules
└── ui/
    ├── dashboard/   Home screen
    ├── ledger/      History list
    ├── analytics/   Trends and interactive chart
    ├── settings/    Configuration and entry sheets
    ├── addentry/    Daily weight sheet
    ├── components/  Shared widgets (keypad, cards, sheets)
    ├── navigation/  Routes and bottom bar
    ├── theme/       Colors, typography
    └── util/        Formatting and share helpers
```

The `domain` package has no Android dependencies, which keeps it easy to unit test.

## Getting started

Requirements: Android Studio (recent stable) with JDK 17+ and Android SDK 36.

```bash
git clone <repo-url>
cd PesoCerto
./gradlew installDebug
```

Or open the project in Android Studio and run the `app` configuration.

## Testing

```bash
./gradlew testDebugUnitTest          # JVM unit tests
./gradlew connectedDebugAndroidTest  # instrumented tests (emulator or device required)
./gradlew assembleRelease            # release build
```

## Privacy

No accounts, no network, no analytics. Weight data is stored locally in a Room database.

## Contributing

Issues and pull requests are welcome. Please keep logic in `domain/`, UI in the matching feature package, and add tests for new domain code.

## License

Released under the [MIT License](LICENSE). The app is fully open source: anyone can use, modify and distribute the code for any purpose.
