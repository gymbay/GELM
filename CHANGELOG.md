# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.0.0] - 2025-02-26

### Added

- **Kotlin Multiplatform (KMP):** the library builds for **Android**, **iOS** (iosX64, iosArm64,
  iosSimulatorArm64), and **JVM**.
- Dependency on KMP ViewModel: `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel` (replacing
  `androidx.lifecycle:lifecycle-viewmodel-ktx`). API is compatible.
- README sections: "Build verification", and dependency examples for all platforms.

### Changed

- **Android API 36:** the library is built with `compileSdk = 36`; for applications it is
  recommended to use `targetSdk = 36`.
- Source code moved to **commonMain**; Android-specific parts (Manifest, consumer-rules) to *
  *androidMain**.
- Tests using `MainDispatcherRule` moved to **androidUnitTest** (new source set layout in Kotlin
  1.9+).
- In `GelmStore`, the thread-safe map of active commands switched from `ConcurrentHashMap` to *
  *Mutex + MutableMap** for all targets.
- Maven publishing: single artifact `io.github.gymbay:gelm` with variants for Android, JVM, and iOS;
  version 2.0.0.
- Minimum Kotlin version — 2.1.x (for compatibility with lifecycle-viewmodel on iOS).

### Fixed

- Build compatibility with JDK 21 via Kotlin update and jvmToolchain configuration.

### Migration / Breaking

- For Android apps the dependency stays the same: `implementation("io.github.gymbay:gelm:2.0.0")`.
  Gradle resolves the Android variant.
- **When versions conflict** with `androidx.lifecycle` in your app, you can exclude the transitive
  dependency and add the desired version manually:

  ```kotlin
  implementation("io.github.gymbay:gelm:2.0.0") {
      exclude(group = "org.jetbrains.androidx.lifecycle", module = "lifecycle-viewmodel")
  }
  implementation("org.jetbrains.androidx.lifecycle:lifecycle-viewmodel:...") // if needed
  ```

- For Compose projects with Kotlin 2.0+,
  the [Compose Compiler](https://plugins.gradle.org/plugin/org.jetbrains.kotlin.plugin.compose)
  plugin is required.

[Unreleased]: https://github.com/gymbay/GELM/compare/v2.0.0...HEAD

[2.0.0]: https://github.com/gymbay/GELM/compare/v1.1.0...v2.0.0
