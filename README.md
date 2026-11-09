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
| `0.1.x` | `>=6.0.0` (Capacitor 6, 7 & 8) | `4.4.0` (Modular) | `24` (Android 7.0) |
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

### 4. Android — Google Play Location Permissions Policy *(Important)*

Some ad network SDKs automatically merge `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION` into your app's merged `AndroidManifest.xml`.

**If your app does NOT use location for its own core functionality**, Google Play Console will reject your release unless you explicitly strip these permissions in your app's `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Remove location permissions merged by ad network SDKs if your app does not use geolocation -->
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" tools:node="remove" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" tools:node="remove" />

    <application ...>
    </application>
</manifest>
```

> **Note:** If your app **does** require geolocation (for example via `@capacitor/geolocation`), do **not** add `tools:node="remove"` and instead declare your location usage in the Google Play Data Safety form.

### 5. Android — AdMob Setup or Removal (`APPLICATION_ID` Auto-Detection vs `appodealExcludedAdapters`)

By default (`appodealProfile = 'standard'`), this plugin includes all direct Appodeal ad networks + BidMachine & Bidon in-app bidding, and **automatically checks your `android/app/src/main/AndroidManifest.xml` at build time**:
- **If `com.google.android.gms.ads.APPLICATION_ID` is present** in `AndroidManifest.xml`, `admob` is compiled and enabled automatically.
- **If `com.google.android.gms.ads.APPLICATION_ID` is NOT present** in `AndroidManifest.xml`, Gradle **automatically excludes `admob`** so your app never crashes on startup even with zero configuration.

#### Option A — You DO use AdMob (How to configure it)
Add your AdMob `APPLICATION_ID` inside `<application>` in `android/app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:networkSecurityConfig="@xml/network_security_config"
        ...>

        <!-- Required by AdMob adapter -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy" />

    </application>
</manifest>
```
*(Don't forget to sync your AdMob account with the [Appodeal AdMob Sync tool](https://amsa-updates.appodeal.com/)).*

#### Option B — You DO NOT use AdMob (How to remove it)
Simply omit the `<meta-data>` tag above (the plugin will auto-exclude `admob` during Gradle sync), or exclude it explicitly in `android/variables.gradle`:

```groovy
// android/variables.gradle
ext {
    // Explicitly excludes AdMob at compile time
    appodealExcludedAdapters = ['admob']
}
```

### 6. Google Play Data Safety & `app-ads.txt`

Before publishing to Google Play:
- Complete the **App Privacy / Data Safety** section in Google Play Console according to [Appodeal's Google Play Privacy Guide](https://docs.appodeal.com/android/data-protection/app-privacy-details).
- Publish your `app-ads.txt` file on your developer website root domain to authorize ad inventory sales.

---

## Configuration — Ad Network Adapters (Appodeal 4.x Modular Architecture)

Starting in **v0.1.0 (Appodeal SDK 4.4.0)**, ad network adapters are completely modular. You can control which networks are compiled into your Android APK/AAB via your app's `android/variables.gradle` (or `android/gradle.properties`) **without modifying the plugin**, and you can also disable networks at runtime from TypeScript.

### 1. Compile-Time Adapter Selection (`android/variables.gradle`)

#### Built-in Profiles (`appodealProfile`)
You can choose a built-in profile with a single property in `android/variables.gradle`:
- `'standard'` *(Default)*: All 26 direct Appodeal ad networks + BidMachine & Bidon in-app bidding (excludes duplicate third-party MAX/LevelPlay waterfall bridges by default to save ~37 AARs of APK weight, and auto-excludes `admob` if `APPLICATION_ID` is absent from `AndroidManifest.xml`).
- `'minimal'`: Lightweight high-fill bundle (`applovin`, `unity_ads`, `vungle`, `mintegral`, `dt_exchange`, `inmobi`, `bidmachine`, `iab`).
- `'full'`: Complete Step 1 bundle including `applovin_max` (+ 21 MAX bridges), `level_play` (+ 16 LevelPlay bridges), and `sentry_analytics`.

```groovy
// android/variables.gradle
ext {
    appodealProfile = 'minimal' // 'standard' (default) | 'minimal' | 'full'
}
```

#### Mode A — Custom Whitelist (`appodealIncludedAdapters`) *(Recommended for Production)*
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
Start with the default universal bundle and remove specific networks you do not want (for example `admob`, `yandex`, `my_target`, or `meta`):

```groovy
// android/variables.gradle
ext {
    appodealExcludedAdapters = ['admob', 'yandex', 'my_target', 'meta']
}
```

#### Mode C — Opt-In Extra Services (`appodealExtraAdapters`)
Add Step 2 UA/Analytics service adapters that are excluded by default (`adjust`, `appsflyer`, `facebook_analytics`, `firebase`):

```groovy
// android/variables.gradle
ext {
    appodealExtraAdapters = ['firebase', 'adjust']
}
```

> **Tip:** You can also pass comma-separated strings in `android/gradle.properties`, e.g.:
> - `appodealExcludedAdapters=admob`
> - `appodealIncludedAdapters=applovin,unity_ads,vungle,mintegral,dt_exchange,inmobi,bidmachine,iab`

### 2. Full Adapter Catalog (Appodeal SDK `4.4.0`)

| Adapter Key (`AppodealNetwork`) | Maven Artifact (`com.appodeal.ads.sdk.adapters:*`) | Version | Default (`standard`) | Notes |
| :--- | :--- | :---: | :---: | :--- |
| `admob` | `admob` | `25.2.0.0` | ✅ Auto | Auto-included if `APPLICATION_ID` is in `AndroidManifest.xml`; auto-excluded otherwise |
| `amazon` | `amazon` | `12.0.0.0` | ✅ Included | Amazon APS |
| `applovin` | `applovin` | `13.6.3.0` | ✅ Included | AppLovin SDK |
| `applovin_max` | `applovin_max` | `13.6.3.1` | ⚙️ `full` / Opt-in | AppLovin MAX mediation (+ auto `com.applovin.mediation:*` bridges) |
| `bidmachine` | `bidmachine` | `3.7.1.0` | ✅ Included | BidMachine (+ auto `io.bidmachine:ads.networks.*` bridges) |
| `bidon` | `bidon` | `0.14.0.0` | ✅ Included | Bidon mediation (+ auto `org.bidon:*-adapter` bridges) |
| `bigo_ads` | `bigo_ads` | `5.9.0.0` | ✅ Included | Bigo Ads |
| `chartboost` | `chartboost` | `9.10.2.0` | ✅ Included | Chartboost |
| `dt_exchange` | `dt_exchange` | `8.4.6.0` | ✅ Included | DT Exchange (Fyber) |
| `iab` | `iab` | `1.8.1.1` | ✅ Included | IAB MRAID / VAST / NAST |
| `inmobi` | `inmobi` | `11.3.0.0` | ✅ Included | InMobi |
| `ironsource` | `ironsource` | `9.4.4.0` | ✅ Included | IronSource |
| `level_play` | `level_play` | `9.4.4.0` | ⚙️ `full` / Opt-in | LevelPlay mediation (+ auto `com.unity3d.ads-mediation:*` bridges) |
| `meta` | `meta` | `6.21.0.0` | ✅ Included | Meta Audience Network |
| `mintegral` | `mintegral` | `17.1.61.0` | ✅ Included | Mintegral |
| `mobilefuse` | `mobilefuse` | `1.11.0.0` | ✅ Included | MobileFuse |
| `moloco` | `moloco` | `4.3.1.0` | ✅ Included | Moloco |
| `my_target` | `my_target` | `5.47.1.0` | ✅ Included | myTarget (VK Ads) |
| `ogury` | `ogury` | `6.2.0.0` | ✅ Included | Ogury |
| `pangle` | `pangle` | `7.7.0.2.0` | ✅ Included | Pangle (TikTok) |
| `pubmatic` | `pubmatic` | `4.10.0.0` | ✅ Included | PubMatic |
| `sentry_analytics` | `sentry_analytics` | `8.44.1.0` | ⚙️ `full` / Opt-in | Sentry crash reporting |
| `smaato` | `smaato` | `22.7.2.0` | ✅ Included | Smaato |
| `startio` | `startio` | `5.2.4.0` | ✅ Included | Start.io |
| `taurusx` | `taurusx` | `1.12.2.0` | ✅ Included | TaurusX |
| `unity_ads` | `unity_ads` | `4.17.0.0` | ✅ Included | Unity Ads |
| `verve` | `verve` | `3.7.1.0` | ✅ Included | Verve |
| `vungle` | `vungle` | `7.7.4.0` | ✅ Included | Liftoff Monetize (Vungle) |
| `yandex` | `yandex` | `7.17.0.0` | ✅ Included | Yandex Ads |
| `admob_next_gen` | `admob_next_gen` | `1.4.0.0` | ❌ Opt-in | Next-Gen Google Mobile Ads (`appodealExtraAdapters`) |
| `adjust` | `adjust` | `5.7.0.0` | ❌ Opt-in (Step 2) | Attribution tracking (`appodealExtraAdapters`) |
| `appsflyer` | `appsflyer` | `6.18.0.1` | ❌ Opt-in (Step 2) | Attribution tracking (`appodealExtraAdapters`) |
| `facebook_analytics` | `facebook_analytics` | `18.3.0.0` | ❌ Opt-in (Step 2) | Meta analytics (`appodealExtraAdapters`) |
| `firebase` | `firebase` | `23.2.0.0` | ❌ Opt-in (Step 2) | Firebase / Remote Config (`appodealExtraAdapters`) |

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
  adTypes:
    AppodealAdType.BANNER |
    AppodealAdType.INTERSTITIAL |
    AppodealAdType.REWARDED_VIDEO,
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

### Rewarded Video & Checking Readiness (`canShow` / `isLoaded`)

Include `AppodealAdType.REWARDED_VIDEO` when calling `initialize()`, check readiness with `canShow()`, and listen for reward completion via `onRewardedVideoFinished`:

```typescript
Appodeal.addListener('onRewardedVideoFinished', ({ amount, currency }) => {
  console.log(`User earned ${amount} ${currency}`);
});

const { canShow } = await Appodeal.canShow({
  adType: AppodealAdType.REWARDED_VIDEO,
  placement: 'double_coins', // optional placement
});

if (canShow) {
  await Appodeal.showRewardedVideo({ placement: 'double_coins' });
}
```

### Ad Revenue Callback (`onAdRevenueReceive`)

Track real-time impression revenue per network and placement:

```typescript
Appodeal.addListener('onAdRevenueReceive', (info) => {
  console.log(
    `[${info.adTypeString}] ${info.networkName} (${info.demandSource}): $${info.revenue} ${info.currency} (${info.precision})`,
  );
});
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

// Rewarded Video events
Appodeal.addListener('onRewardedVideoLoaded', ({ isPrecache }) => {
  console.log('Rewarded video ready, precache:', isPrecache);
});
Appodeal.addListener('onRewardedVideoFailedToLoad', () => { /* ... */ });
Appodeal.addListener('onRewardedVideoShown', () => { /* ... */ });
Appodeal.addListener('onRewardedVideoShowFailed', () => { /* ... */ });
Appodeal.addListener('onRewardedVideoClicked', () => { /* ... */ });
Appodeal.addListener('onRewardedVideoFinished', ({ amount, currency }) => {
  console.log('Reward granted:', amount, currency);
});
Appodeal.addListener('onRewardedVideoClosed', ({ isFinished }) => {
  console.log('Rewarded video closed, finished:', isFinished);
});

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
| `isLoaded(options)` | `Promise<{ isLoaded: boolean }>` | Check if `adType` is currently loaded |
| `canShow(options)` | `Promise<{ canShow: boolean }>` | Check if `adType` can be shown (optional `placement`) |
| `showBanner(options?)` | `Promise<void>` | Show banner ad (`position`: `BANNER_BOTTOM` or `BANNER_TOP`, optional `placement`) |
| `hideBanner()` | `Promise<void>` | Hide banner ad |
| `showInterstitial(options?)` | `Promise<void>` | Show interstitial (optional `placement`; rejects if not loaded) |
| `showRewardedVideo(options?)` | `Promise<void>` | Show rewarded video (optional `placement`; rejects if not loaded) |
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

#### Rewarded Video

| Event | Payload | Description |
| :--- | :--- | :--- |
| `onRewardedVideoLoaded` | `{ isPrecache: boolean }` | Ad ready to show |
| `onRewardedVideoFailedToLoad` | `{}` | Ad failed to load |
| `onRewardedVideoShown` | `{}` | Ad playback started |
| `onRewardedVideoShowFailed` | `{}` | Ad loaded but failed to display |
| `onRewardedVideoClicked` | `{}` | User tapped the ad |
| `onRewardedVideoFinished` | `{ amount: number, currency: string }` | User completed watching and earned reward |
| `onRewardedVideoClosed` | `{ isFinished: boolean }` | User dismissed the ad (`isFinished` indicates completion) |

#### Ad Revenue

| Event | Payload | Description |
| :--- | :--- | :--- |
| `onAdRevenueReceive` | `AdRevenueEvent` (`{ adType, adTypeString, networkName, demandSource, adUnitName, placement, revenue, currency, precision }`) | Impression-level ad revenue data |

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the repo
2. Create your feature branch: `git checkout -b feat/my-feature`
3. Commit your changes following [Conventional Commits](https://www.conventionalcommits.org/)
4. Open a Pull Request

## License

[MIT](LICENSE)
