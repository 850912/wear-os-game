# Wear Compose dependency-resolution fix — 2026-10-01

The CI log still resolved `androidx.wear.compose` 1.7.0 even though the app declared 1.6.2 directly.

## Fix

- Keep direct Wear Compose dependencies at 1.6.2.
- Explicitly add `compose-material-core:1.6.2`.
- Force `compose-material3`, `compose-foundation`, and `compose-material-core` to 1.6.2 for every app configuration so transitive dependency resolution cannot upgrade them to 1.7.x.
- Change the quality CI build from `assembleDebug` to `assembleRelease`; lint and release unit tests remain enabled.

This preserves the existing `compileSdk 35` + Android Gradle Plugin `8.6.1` toolchain while producing the Release build requested for performance testing.
