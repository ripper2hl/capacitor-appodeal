# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
