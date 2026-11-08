# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [0.1.0] - 2026-11-08

### Added
- **Appodeal SDK 4.x Modular Adapter Architecture:** Full catalog of 34 official Appodeal `4.4.0` adapters (`com.appodeal.ads.sdk.adapters:*`) configurable at compile-time via `appodealIncludedAdapters` (whitelist), `appodealExcludedAdapters` (blacklist), and `appodealExtraAdapters` (opt-in) in `android/variables.gradle` or `android/gradle.properties`.
- Automatic resolution of BidMachine (`io.bidmachine:ads.networks.*`), Bidon (`org.bidon:*-adapter`), AppLovin MAX (`com.applovin.mediation:*-adapter`), and LevelPlay (`com.unity3d.ads-mediation:*-adapter`) mediation bridges when active alongside their respective demand networks.
- `AppodealNetwork` enum in TypeScript with all 28 ad network identifiers.
- `disableNetwork({ network, adTypes? })` method and `disabledNetworks?: string[]` option in `initialize()` for runtime network control before initialization.
- `getNetworks({ adTypes? })` method to inspect active networks at runtime.
- **Rewarded Video support:** `showRewardedVideo()` method (rejects if not loaded) and all 7 native callbacks (`onRewardedVideoLoaded`, `onRewardedVideoFailedToLoad`, `onRewardedVideoShown`, `onRewardedVideoShowFailed`, `onRewardedVideoClicked`, `onRewardedVideoFinished` with `{ amount, currency }`, and `onRewardedVideoClosed` with `{ isFinished }`), plus typed interfaces (`RewardedVideoLoadedEvent`, `RewardedVideoFinishedEvent`, `RewardedVideoClosedEvent`).
- Unit test suite for both Android (`JUnit` + `Mockito` + `JaCoCo`) and Web/TypeScript (`node:test` + LCOV) integrated with SonarQube (`sonar-project.properties`).

### Changed
- **BREAKING (Android minSdk):** Migrated native Android dependency from legacy monolithic `com.appodeal.ads:sdk:3.12.0.1` to modular `com.appodeal.ads.sdk:core:4.4.0`. Minimum supported Android API level is now **24 (Android 7.0)**.
- Default `compileSdkVersion` and `targetSdkVersion` updated to `36` with Gradle `8.14.3` and Android Gradle Plugin `8.13.0` (16 KB page size compatible).
- Default included adapters match the official **Appodeal 4.4.0 Step 1 Get Started bundle** (all 29 ad network/mediation adapters included by default, including `admob`, `applovin`, `applovin_max`, `bidmachine`, `bidon`, `level_play`, `unity_ads`, `vungle`, `mintegral`, `dt_exchange`, `inmobi`, `meta`, etc.), while **Step 2 UA/Analytics services** (`adjust`, `appsflyer`, `facebook_analytics`, `firebase`) remain excluded by default (same as `v0.0.3`). Any network (such as `admob`) can be easily removed via `appodealExcludedAdapters = ['admob']` or `appodealIncludedAdapters`.
- Hardened `testing` flag in `initialize()` to strictly default to `false` with explicit log level management (`LogLevel.none` in production, warning + `LogLevel.verbose` when `testing: true`).

---

## [0.0.3] - 2026-09-20

### Added
- Typed event payload interfaces: `BannerLoadedEvent`, `InterstitialLoadedEvent`, `EmptyEvent`.
- JSDoc comments on all public methods and `AppodealAdType` enum members.
- `removeAllListeners()` declaration in `AppodealPlugin` interface.
- `showBanner()` now accepts an optional `options` object (`{ position?: number }`) for consistency.

### Fixed
- `package.json` `files` field was including `android/build/` (64 MB of Gradle artifacts).
  Package size reduced from **64.7 MB / 6,172 files** to **~99 kB / 22 files**.
- README example for `showBanner()` had incorrect signature (no arguments).

### Changed
- README rewritten: added API reference tables, event payload documentation,
  correct usage examples, and badges.
- `AppodealAdType.NON_SKIPPABLE_VIDEO` shares value `128` with `REWARDED_VIDEO`
  (intentional — matches Appodeal SDK behavior, now documented in enum).

---

## [0.0.2] - 2026-09-XX

### Added
- Google Play Billing Library `8.0.0` forced dependency to comply with August 2026 policy.
- Test ads support via `testing` option in `initialize()`.

### Changed
- Updated README with installation steps and Network Security Config.

---

## [0.0.1] - 2026-09-XX

### Added
- Initial release.
- Android native implementation with Appodeal SDK `3.12.0.1`.
- Web mock implementation for development/testing.
- `initialize()`, `showBanner()`, `hideBanner()`, `showInterstitial()` methods.
- Banner and Interstitial event listeners.
- Excluded adapters by default: `adjust`, `appsflyer`, `facebook_analytics`, `firebase`.
