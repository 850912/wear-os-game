# v8.0.2 AAR metadata compatibility fix — 2026-10-01

## Failure

CI failed at `:app:checkDebugAarMetadata` because Wear Compose `1.7.0` requires:

- `compileSdk >= 37`
- Android Gradle Plugin `>= 9.1.0`

The project intentionally uses the existing compatible build chain:

- Android Gradle Plugin `8.6.1`
- Gradle / CI runtime `8.7`
- JDK `17`
- `compileSdk 35`
- `targetSdk 35`

## Fix

Reverted only these dependencies from `1.7.0` to `1.6.2`:

```gradle
implementation "androidx.wear.compose:compose-material3:1.6.2"
implementation "androidx.wear.compose:compose-foundation:1.6.2"
```

Wear Compose `1.6.2` is the compatibility baseline already used by this source tree, and the existing Kotlin source contains the corresponding API/import fixes.

No gameplay code, save format, game catalog, application ID, SDK target, signing behavior, or release minification settings were changed.
