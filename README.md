# capacitor-appodeal

**Unofficial** Capacitor plugin for [Appodeal](https://appodeal.com) Mediation Only integration.

> **Disclaimer:** This project is not affiliated with, endorsed by, or connected to Appodeal. It is an open-source community plugin.

[![npm version](https://img.shields.io/npm/v/capacitor-appodeal.svg)](https://www.npmjs.com/package/capacitor-appodeal)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## Supported Platforms

| Platform | Supported | SDK Version | Notes |
| :--- | :---: | :---: | :--- |
| **Android** | ✅ | `4.4.0` | Native modular implementation (`minSdk 24`) |
| **Web** | ✅ | Mock | Development/Testing only |
| **iOS** | ❌ | — | Not implemented yet |

## Compatibility

| Plugin Version | Capacitor Version | Appodeal Android SDK | Min Android API |
| :--- | :--- | :--- | :---: |
| `0.1.x` | `^6.0.0` (compatible with 6–8) | `4.4.0` (Modular) | `24` (Android 7.0) |
| `0.0.x` | `^6.0.0` | `3.12.0.1` (Legacy bundle) | `22` |

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

## Configuration — Ad Network Adapters (Appodeal 4.x Modular Architecture)

Starting in **v0.1.0 (Appodeal SDK 4.4.0)**, ad network adapters are completely modular. You can control which networks are compiled into your Android APK/AAB via your app's `android/variables.gradle` (or `android/gradle.properties`) **without modifying the plugin**, and you can also disable networks at runtime from TypeScript.

### 1. Compile-Time Adapter Selection (`android/variables.gradle`)

#### Mode A — Whitelist (`appodealIncludedAdapters`) *(Recommended for Production)*
Compile **only** the exact ad networks that provide demand for your app. This minimizes APK/AAB size and avoids bundling unused SDKs:

```groovy
// android/variables.gradle
ext {
    minSdkVersion = 24
    compileSdkVersion = 36
    targetSdkVersion = 36
    // ...

    // Only these networks (plus automatic BidMachine/Bidon bridges if included) will be compiled:
    appodealIncludedAdapters = [
        'applovin',
        'unity_ads',
        'vungle',
        'mintegral',
        'dt_exchange',
        'inmobi',
        'bidmachine',
        'iab'
    ]
}
```

#### Mode B — Exclude Specific Networks (`appodealExcludedAdapters`)
Start with the default included networks and remove specific ones:

```groovy
// android/variables.gradle
ext {
    appodealExcludedAdapters = ['yandex', 'my_target', 'meta']
}
```

#### Mode C — Opt-In Extra Adapters (`appodealExtraAdapters`)
Add adapters that are excluded by default (such as `admob` or analytics trackers):

```groovy
// android/variables.gradle
ext {
    appodealExtraAdapters = ['admob']
}
```

> **Tip:** You can also pass comma-separated strings in `android/gradle.properties`, e.g.:
> `appodealIncludedAdapters=applovin,unity_ads,vungle,mintegral,dt_exchange,inmobi,bidmachine,iab`

### 2. Full Adapter Catalog (Appodeal SDK `4.4.0`)

| Adapter Key (`AppodealNetwork`) | Maven Artifact (`com.appodeal.ads.sdk.adapters:*`) | Version | Default State | Notes |
| :--- | :--- | :---: | :---: | :--- |
| `applovin` | `applovin` | `13.6.3.0` | ✅ Included | AppLovin SDK |
| `unity_ads` | `unity_ads` | `4.17.0.0` | ✅ Included | Unity Ads |
| `vungle` | `vungle` | `7.7.4.0` | ✅ Included | Liftoff Monetize (Vungle) |
| `mintegral` | `mintegral` | `17.1.61.0` | ✅ Included | Mintegral |
| `dt_exchange` | `dt_exchange` | `8.4.6.0` | ✅ Included | DT Exchange (Fyber) |
| `inmobi` | `inmobi` | `11.3.0.0` | ✅ Included | InMobi |
| `bidmachine` | `bidmachine` | `3.7.1.0` | ✅ Included | BidMachine (+ auto bridges for active networks) |
| `iab` | `iab` | `1.8.1.1` | ✅ Included | IAB MRAID / VAST / NAST |
| `bidon` | `bidon` | `0.14.0.0` | ✅ Included | Bidon mediation (+ auto bridges) |
| `bigo_ads` | `bigo_ads` | `5.9.0.0` | ✅ Included | Bigo Ads |
| `chartboost` | `chartboost` | `9.10.2.0` | ✅ Included | Chartboost |
| `ironsource` | `ironsource` | `9.4.4.0` | ✅ Included | IronSource |
| `meta` | `meta` | `6.21.0.0` | ✅ Included | Meta Audience Network |
| `mobilefuse` | `mobilefuse` | `1.11.0.0` | ✅ Included | MobileFuse |
| `moloco` | `moloco` | `4.3.1.0` | ✅ Included | Moloco |
| `my_target` | `my_target` | `5.47.1.0` | ✅ Included | myTarget (VK Ads) |
| `ogury` | `ogury` | `6.2.0.0` | ✅ Included | Ogury |
| `pangle` | `pangle` | `7.7.0.2.0` | ✅ Included | Pangle (TikTok) |
| `pubmatic` | `pubmatic` | `4.10.0.0` | ✅ Included | PubMatic |
| `smaato` | `smaato` | `22.7.2.0` | ✅ Included | Smaato |
| `startio` | `startio` | `5.2.4.0` | ✅ Included | Start.io |
| `taurusx` | `taurusx` | `1.12.2.0` | ✅ Included | TaurusX |
| `verve` | `verve` | `3.7.1.0` | ✅ Included | Verve |
| `yandex` | `yandex` | `7.17.0.0` | ✅ Included | Yandex Ads |
| `admob` | `admob` | `25.2.0.0` | ❌ Excluded | Requires `APPLICATION_ID` in `AndroidManifest.xml` |
| `admob_next_gen` | `admob_next_gen` | `1.4.0.0` | ❌ Excluded | Next-Gen Google Mobile Ads |
| `amazon` | `amazon` | `12.0.0.0` | ❌ Excluded | Amazon APS (requires MobileAds or stub) |
| `applovin_max` | `applovin_max` | `13.6.3.1` | ❌ Excluded | Secondary MAX mediation |
| `level_play` | `level_play` | `9.4.4.0` | ❌ Excluded | Secondary LevelPlay mediation |
| `adjust` | `adjust` | `5.7.0.0` | ❌ Excluded | Attribution tracking |
| `appsflyer` | `appsflyer` | `6.18.0.1` | ❌ Excluded | Attribution tracking |
| `facebook_analytics` | `facebook_analytics` | `18.3.0.0` | ❌ Excluded | Meta analytics |
| `firebase` | `firebase` | `23.2.0.0` | ❌ Excluded | Firebase / Google Ads components |
| `sentry_analytics` | `sentry_analytics` | `8.44.1.0` | ❌ Excluded | Sentry crash reporting |

---

## Usage

### Initialize (and Safe Test Mode)

Call `initialize` once, early in your app lifecycle (e.g., `AppComponent` constructor or `app.component.ts`).

> **⚠️ Important — Avoid Test Ads in Production:**
> `testing` defaults to `false`. Always bind `testing` to your framework's build environment (e.g. `!environment.production`) so you never accidentally ship `testing: true` to Google Play.

```typescript
import { Appodeal, AppodealAdType, AppodealNetwork } from 'capacitor-appodeal';
import { environment } from '../environments/environment';

await Appodeal.initialize({
  appKey: 'YOUR_APP_KEY',
  adTypes: AppodealAdType.BANNER | AppodealAdType.INTERSTITIAL,
  testing: !environment.production, // false in production builds!
  // Optional: disable specific networks at runtime before initialization
  disabledNetworks: [AppodealNetwork.META, AppodealNetwork.YANDEX],
});
```

### Runtime Network Management (`disableNetwork` / `getNetworks`)

```typescript
// Disable a network for all ad types (call BEFORE initialize)
await Appodeal.disableNetwork({ network: AppodealNetwork.MY_TARGET });

// Disable a network only for banners (call BEFORE initialize)
await Appodeal.disableNetwork({
  network: AppodealNetwork.INMOBI,
  adTypes: AppodealAdType.BANNER,
});

// Inspect registered networks
const { networks } = await Appodeal.getNetworks();
console.log('Active Appodeal networks:', networks);
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
| `initialize(options)` | `Promise<void>` | Initialize the SDK (`appKey`, `adTypes`, optional `testing`, optional `disabledNetworks`) |
| `disableNetwork(options)` | `Promise<void>` | Disable an ad network before initialization (`network`, optional `adTypes`) |
| `getNetworks(options?)` | `Promise<{ networks: string[] }>` | Get registered ad networks (optional `adTypes` filter) |
| `showBanner(options?)` | `Promise<void>` | Show banner ad (`position`: `BANNER_BOTTOM` or `BANNER_TOP`) |
| `hideBanner()` | `Promise<void>` | Hide banner ad |
| `showInterstitial()` | `Promise<void>` | Show interstitial (rejects if not loaded) |
| `addListener(event, fn)` | `Promise<PluginListenerHandle>` | Register an event listener |
| `removeAllListeners()` | `Promise<void>` | Remove all event listeners |

### `AppodealAdType` enum

| Value | Constant | Notes |
| :---: | :--- | :--- |
| `0` | `NONE` | |
| `3` | `INTERSTITIAL` | |
| `4` | `BANNER` | |
| `8` | `BANNER_BOTTOM` | Use with `showBanner()` |
| `16` | `BANNER_TOP` | Use with `showBanner()` |
| `64` | `BANNER_VIEW` | |
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
