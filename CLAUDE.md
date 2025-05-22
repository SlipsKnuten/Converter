# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is an Android application called "Converter" - an interactive unit conversion tool with military-themed visual elements. The app converts between metric and imperial units for weight, length, and volume measurements with unique visual feedback including explosion animations and screen shake effects.

## Build Commands

All commands should be run from the project root directory using the gradle wrapper:

```bash
# Build commands
./gradlew clean build          # Clean and build entire project
./gradlew assembleDebug        # Build debug APK
./gradlew assembleRelease      # Build release APK
./gradlew bundleRelease        # Build release bundle (AAB)

# Running the app
./gradlew installDebug         # Install and run on connected device/emulator
./gradlew uninstallDebug       # Uninstall debug version

# Testing
./gradlew test                 # Run all unit tests
./gradlew connectedAndroidTest # Run instrumentation tests on device
./gradlew check               # Run all checks (tests + lint)

# Code quality
./gradlew lint                # Run lint checks
./gradlew lintFix            # Auto-fix lint issues where possible
```

## Architecture Overview

### Core Components

1. **MainActivity.kt** - The single activity containing all converter logic:
   - Manages 4 converters (kg→lb, cm→in, L→gal, m→ft)
   - Handles theme switching with persistence (SharedPreferences)
   - Implements visual effects (explosions, screen shake)
   - Uses `ConverterConfig` data class for converter configuration
   - Bidirectional sync between EditText inputs and SeekBars

2. **Particle System** (prepared but not currently used):
   - `Particle.kt` - Particle data model
   - `ParticleView.kt` - Custom view with coroutine-based animation

### Key Implementation Details

- **Theme Management**: Light/dark mode toggle persisted in SharedPreferences
- **Visual Effects**: 
  - Explosion GIFs follow SeekBar thumb position
  - Screen shake intensity based on slider movement speed
  - Military-themed SeekBar thumbs (pistol, rifle, jet, tank)
- **Input Handling**: Automatic unit suffix management with validation
- **Dependencies**: Minimal external deps - mainly Glide for GIF handling

### Resource Structure

- **Layouts**: Single `activity_main.xml` with FrameLayout root
- **Military Assets**: pistol.png, m4.png, f35.png, tank.png
- **Animations**: explosion.gif for visual feedback
- **Themes**: Light/dark themes in values/ and values-night/

### Configuration

- **Package**: com.example.converter
- **Min SDK**: 21 (Android 5.0)
- **Target SDK**: 34 (Android 14)
- **Build System**: Gradle 8.11.1 with Kotlin DSL
- **Version Catalog**: gradle/libs.versions.toml defines all dependencies