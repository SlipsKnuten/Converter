# Converter

An interactive Android unit conversion app.

## Features

- **Multiple Unit Conversions**:
  - Weight: Kilograms ↔ Pounds
  - Length: Centimeters ↔ Inches, Meters ↔ Feet
  - Volume: Liters ↔ Gallons

- **Interactive Design**:
  - Synchronized seekbars and text input fields
  - Real-time bidirectional conversion

## Technical Details

- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: Single Activity with data binding
- **Dependencies**: Minimal - primarily Glide for GIF handling

## Download

### For Android Users
1. Visit the [Releases page](https://github.com/SlipsKnuten/Converter/releases)
2. Download the latest `converter-vX.X.apk`
3. Open the APK on your phone
4. Enable "Install unknown apps" if prompted
5. Install and enjoy!

## Building from Source

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run all tests
./gradlew test
```

## License

This project is open source. See LICENSE file for details.

## Credits

Flag icons from Pixabay:
- [American Flag](https://pixabay.com/sv/vectors/amerikanska-flaggan-usa-flagga-2144392/)
- [European Flag](https://pixabay.com/sv/vectors/europa-europeiska-unionen-flagga-155191/)