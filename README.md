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

### 5. Android — AdMob Setup or Removal (`APPLICATION_ID` vs `appodealExcludedAdapters`)

By default, this plugin includes the full **Step 1 Appodeal 4.4.0 Mediation Bundle** (including `admob`), while keeping **Step 2 UA/Analytics Services** (`adjust`, `appsflyer`, `facebook_analytics`, `firebase`) excluded by default.

Depending on whether you use AdMob in your app, choose **one** of the two options below:

#### Option A — You DO use AdMob (How to configure it)
Keep the default adapters and add your AdMob `APPLICATION_ID` inside `<application>` in `android/app/src/main/AndroidManifest.xml`:

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
If you don't use AdMob (or don't want to add `com.google.android.gms.ads.APPLICATION_ID`), exclude `admob` in `android/variables.gradle` (or use `appodealIncludedAdapters` to whitelist only your active networks):

```groovy
// android/variables.gradle
ext {
    // Excludes AdMob at compile time — no APPLICATION_ID meta-data needed in AndroidManifest.xml
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

| Adapter Key (`AppodealNetwork`) | Maven Artifact (`com.appodeal.ads.sdk.adapters:*`) | Version | Default State | Notes |
| :--- | :--- | :---: | :---: | :--- |
| `admob` | `admob` | `25.2.0.0` | ✅ Included (Step 1) | Requires `APPLICATION_ID` in `AndroidManifest.xml` (or remove via `appodealExcludedAdapters = ['admob']`) |
| `amazon` | `amazon` | `12.0.0.0` | ✅ Included (Step 1) | Amazon APS |
| `applovin` | `applovin` | `13.6.3.0` | ✅ Included (Step 1) | AppLovin SDK |
| `applovin_max` | `applovin_max` | `13.6.3.1` | ✅ Included (Step 1) | AppLovin MAX mediation (+ auto `com.applovin.mediation:*` bridges) |
| `bidmachine` | `bidmachine` | `3.7.1.0` | ✅ Included (Step 1) | BidMachine (+ auto `io.bidmachine:ads.networks.*` bridges) |
| `bidon` | `bidon` | `0.14.0.0` | ✅ Included (Step 1) | Bidon mediation (+ auto `org.bidon:*-adapter` bridges) |
| `bigo_ads` | `bigo_ads` | `5.9.0.0` | ✅ Included (Step 1) | Bigo Ads |
| `chartboost` | `chartboost` | `9.10.2.0` | ✅ Included (Step 1) | Chartboost |
| `dt_exchange` | `dt_exchange` | `8.4.6.0` | ✅ Included (Step 1) | DT Exchange (Fyber) |
| `iab` | `iab` | `1.8.1.1` | ✅ Included (Step 1) | IAB MRAID / VAST / NAST |
| `inmobi` | `inmobi` | `11.3.0.0` | ✅ Included (Step 1) | InMobi |
| `ironsource` | `ironsource` | `9.4.4.0` | ✅ Included (Step 1) | IronSource |
| `level_play` | `level_play` | `9.4.4.0` | ✅ Included (Step 1) | LevelPlay mediation (+ auto `com.unity3d.ads-mediation:*` bridges) |
| `meta` | `meta` | `6.21.0.0` | ✅ Included (Step 1) | Meta Audience Network |
| `mintegral` | `mintegral` | `17.1.61.0` | ✅ Included (Step 1) | Mintegral |
| `mobilefuse` | `mobilefuse` | `1.11.0.0` | ✅ Included (Step 1) | MobileFuse |
| `moloco` | `moloco` | `4.3.1.0` | ✅ Included (Step 1) | Moloco |
| `my_target` | `my_target` | `5.47.1.0` | ✅ Included (Step 1) | myTarget (VK Ads) |
| `ogury` | `ogury` | `6.2.0.0` | ✅ Included (Step 1) | Ogury |
| `pangle` | `pangle` | `7.7.0.2.0` | ✅ Included (Step 1) | Pangle (TikTok) |
| `pubmatic` | `pubmatic` | `4.10.0.0` | ✅ Included (Step 1) | PubMatic |
| `sentry_analytics` | `sentry_analytics` | `8.44.1.0` | ✅ Included (Step 1) | Sentry crash reporting |
| `smaato` | `smaato` | `22.7.2.0` | ✅ Included (Step 1) | Smaato |
| `startio` | `startio` | `5.2.4.0` | ✅ Included (Step 1) | Start.io |
| `taurusx` | `taurusx` | `1.12.2.0` | ✅ Included (Step 1) | TaurusX |
| `unity_ads` | `unity_ads` | `4.17.0.0` | ✅ Included (Step 1) | Unity Ads |
| `verve` | `verve` | `3.7.1.0` | ✅ Included (Step 1) | Verve |
| `vungle` | `vungle` | `7.7.4.0` | ✅ Included (Step 1) | Liftoff Monetize (Vungle) |
| `yandex` | `yandex` | `7.17.0.0` | ✅ Included (Step 1) | Yandex Ads |
| `admob_next_gen` | `admob_next_gen` | `1.4.0.0` | ❌ Excluded | Next-Gen Google Mobile Ads (opt-in) |
| `adjust` | `adjust` | `5.7.0.0` | ❌ Excluded (Step 2) | Attribution tracking |
| `appsflyer` | `appsflyer` | `6.18.0.1` | ❌ Excluded (Step 2) | Attribution tracking |
| `facebook_analytics` | `facebook_analytics` | `18.3.0.0` | ❌ Excluded (Step 2) | Meta analytics |
| `firebase` | `firebase` | `23.2.0.0` | ❌ Excluded (Step 2) | Firebase / Remote Config |

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
