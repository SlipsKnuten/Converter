# Release Guide

## Creating a New Release

### 1. Update Version
In `app/build.gradle.kts`, update:
```kotlin
versionCode = 2  // Increment by 1
versionName = "1.1"  // Update as needed
```

### 2. Build Release APK Locally (Optional)
```bash
./gradlew clean assembleRelease
```

### 3. Create GitHub Release
1. Go to your repository on GitHub
2. Click "Releases" → "Create a new release"
3. Create a new tag (e.g., `v1.1`)
4. Fill in release notes highlighting new features
5. GitHub Actions will automatically build and attach the APK

## Manual APK Signing (For Google Play)

If you need a signed APK for Google Play:

1. Generate a keystore (first time only):
```bash
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

2. Sign the APK:
```bash
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore release-key.jks app/build/outputs/apk/release/app-release-unsigned.apk my-key-alias
```

3. Align the APK:
```bash
zipalign -v 4 app/build/outputs/apk/release/app-release-unsigned.apk app-release-signed.apk
```

## Distribution Channels

### GitHub Releases (Recommended)
- Automated builds via GitHub Actions
- Direct APK downloads
- Version history
- Release notes

### Google Play Store
- Requires signed APK/AAB
- Wider audience reach
- Automatic updates
- Requires $25 developer fee

### F-Droid
- Open source app store
- No fees required
- Automated builds from source
- Submit via: https://f-droid.org/en/contribute/

### Direct APK Distribution
- Host on your website
- Share via cloud storage
- Email to users
- Note: Users need "Unknown Sources" enabled