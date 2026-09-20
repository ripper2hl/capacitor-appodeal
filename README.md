# capacitor-appodeal

**Unofficial** Capacitor plugin for [Appodeal](https://appodeal.com) Mediation Only integration.

> **Disclaimer:** This project is not affiliated with, endorsed by, or connected to Appodeal. It is an open-source community plugin.

[![npm version](https://img.shields.io/npm/v/capacitor-appodeal.svg)](https://www.npmjs.com/package/capacitor-appodeal)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Supported Platforms

| Platform | Supported | SDK Version | Notes |
| :--- | :---: | :---: | :--- |
| **Android** | ✅ | `3.12.0.1` | Native implementation |
| **Web** | ✅ | Mock | Development/Testing only |
| **iOS** | ❌ | — | Not implemented yet |

## Compatibility

| Plugin Version | Capacitor Version |
| :--- | :--- |
| `0.0.x` | `^6.0.0` |

---

## Installation

### 1. Install the package

```bash
npm install capacitor-appodeal
npx cap sync
```

### 2. Android — add the Appodeal Maven repository (REQUIRED)

The Appodeal SDK is hosted on a custom Maven repository. Add it to your **project-level** `android/build.gradle`:

```groovy
// android/build.gradle
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url "https://artifactory.appodeal.com/appodeal" } // ← add this
    }
}
```

### 3. Android — Network Security Config *(recommended)*

Some ad networks require cleartext HTTP traffic. Create `android/app/src/main/res/xml/network_security_config.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">localhost</domain>
    </domain-config>
</network-security-config>
```

Then reference it in `android/app/src/main/AndroidManifest.xml`:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
</application>
```

---

## Configuration

### Excluded ad network adapters

By default the following adapters are **excluded** to avoid compliance/tracking issues and reduce APK size:

| Adapter | Reason excluded |
| :--- | :--- |
| `adjust` | Analytics/attribution tracking |
| `appsflyer` | Analytics/attribution tracking |
| `facebook_analytics` | Meta tracking |
| `firebase` | Includes AdMob/Google Ads components |

If you need any of these, add them manually to your app's `android/app/build.gradle`.

---

## Usage

### Initialize

Call `initialize` once, early in your app lifecycle (e.g., `AppComponent` constructor or `app.component.ts`).

```typescript
import { Appodeal, AppodealAdType } from 'capacitor-appodeal';

await Appodeal.initialize({
  appKey: 'YOUR_APP_KEY',
  adTypes: AppodealAdType.BANNER | AppodealAdType.INTERSTITIAL,
  testing: true, // omit or set false in production
});
```

### Banner

```typescript
// Show banner at the bottom (default)
await Appodeal.showBanner();

// Show banner at the top
await Appodeal.showBanner({ position: AppodealAdType.BANNER_TOP });

// Hide banner
await Appodeal.hideBanner();
```

### Interstitial

Wait for the `onInterstitialLoaded` event before calling `showInterstitial()`.

```typescript
const handle = await Appodeal.addListener('onInterstitialLoaded', async () => {
  await handle.remove();
  await Appodeal.showInterstitial();
});
```

Or check readiness defensively:

```typescript
try {
  await Appodeal.showInterstitial();
} catch {
  console.warn('Interstitial not ready yet');
}
```

### Listeners

```typescript
// Banner events
Appodeal.addListener('onBannerLoaded', ({ height, isPrecache }) => {
  console.log('Banner loaded, height:', height);
});
Appodeal.addListener('onBannerFailedToLoad', () => { /* ... */ });
Appodeal.addListener('onBannerShown', () => { /* ... */ });
Appodeal.addListener('onBannerClicked', () => { /* ... */ });
Appodeal.addListener('onBannerShowFailed', () => { /* ... */ });

// Interstitial events
Appodeal.addListener('onInterstitialLoaded', ({ isPrecache }) => {
  console.log('Interstitial ready, precache:', isPrecache);
});
Appodeal.addListener('onInterstitialFailedToLoad', () => { /* ... */ });
Appodeal.addListener('onInterstitialShown', () => { /* ... */ });
Appodeal.addListener('onInterstitialShowFailed', () => { /* ... */ });
Appodeal.addListener('onInterstitialClicked', () => { /* ... */ });
Appodeal.addListener('onInterstitialClosed', () => { /* ... */ });

// Clean up all listeners at once
await Appodeal.removeAllListeners();
```

---

## API Reference

### Methods

| Method | Returns | Description |
| :--- | :--- | :--- |
| `initialize(options)` | `Promise<void>` | Initialize the SDK |
| `showBanner(options?)` | `Promise<void>` | Show banner ad |
| `hideBanner()` | `Promise<void>` | Hide banner ad |
| `showInterstitial()` | `Promise<void>` | Show interstitial (rejects if not loaded) |
| `addListener(event, fn)` | `Promise<PluginListenerHandle>` | Register an event listener |
| `removeAllListeners()` | `Promise<void>` | Remove all event listeners |

### `AppodealAdType` enum

| Value | Constant | Notes |
| :---: | :--- | :--- |
| `3` | `INTERSTITIAL` | |
| `4` | `BANNER` | |
| `8` | `BANNER_BOTTOM` | Use with `showBanner()` |
| `16` | `BANNER_TOP` | Use with `showBanner()` |
| `128` | `REWARDED_VIDEO` / `NON_SKIPPABLE_VIDEO` | Same value in the Appodeal SDK |
| `256` | `MREC` | |
| `512` | `NATIVE` | |

### Events

#### Banner

| Event | Payload | Description |
| :--- | :--- | :--- |
| `onBannerLoaded` | `{ height: number, isPrecache: boolean }` | Ad loaded |
| `onBannerFailedToLoad` | `{}` | Ad failed to load |
| `onBannerShown` | `{}` | Ad became visible |
| `onBannerClicked` | `{}` | User tapped the banner |
| `onBannerShowFailed` | `{}` | Ad loaded but failed to display |

#### Interstitial

| Event | Payload | Description |
| :--- | :--- | :--- |
| `onInterstitialLoaded` | `{ isPrecache: boolean }` | Ad ready to show |
| `onInterstitialFailedToLoad` | `{}` | Ad failed to load |
| `onInterstitialShown` | `{}` | Ad displayed |
| `onInterstitialShowFailed` | `{}` | Ad loaded but failed to display |
| `onInterstitialClicked` | `{}` | User tapped the ad |
| `onInterstitialClosed` | `{}` | User dismissed the ad |

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the repo
2. Create your feature branch: `git checkout -b feat/my-feature`
3. Commit your changes following [Conventional Commits](https://www.conventionalcommits.org/)
4. Open a Pull Request

## License

[MIT](LICENSE)
