# capacitor-appodeal

**Unofficial** Capacitor plugin for Appodeal Mediation Only integration. 

This plugin allows you to integrate Appodeal ads into your Capacitor application. It is designed for "Mediation Only" usage, meaning it provides the core SDK.

> **Disclaimer:** This project is not affiliated with, endorsed by, or connected to Appodeal. It is an open-source community plugin.

## Supported Platforms

- Android
- Web (Mock only)
- iOS (Not implemented yet - Contributions welcome!)

## Installation

```bash
npm install capacitor-appodeal
npx cap sync
```

## Configuration

### Android

#### Excluded Adapters
By default, this plugin initializes the Appodeal SDK with the following adapters **excluded** to prevent tracking/compliance issues and size bloat:
- `adjust`
- `appsflyer`
- `facebook_analytics`
- `firebase` (This often includes AdMob/Google Ads components)

If you need these adapters, you must manually add them to your app's `build.gradle` dependencies or modify the plugin's `build.gradle` if you fork it.


## Android Configuration (Critical)

### 1. Network Security Config
Appodeal requires allowing cleartext traffic for some ad networks.
Create a file at `android/app/src/main/res/xml/network_security_config.xml` in your **main application** (not the plugin) with the following content:

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

### 2. Update AndroidManifest.xml
Open your **main application's** `android/app/src/main/AndroidManifest.xml` and add the `networkSecurityConfig` attribute to the `<application>` tag:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ... >
    <!-- ... -->
</application>
```

### 3. Mediation Adapters
Since this plugin uses "Mediation Only" mode, ensure you have configured your adapters correctly if you have additional manual requirements, although the `build.gradle` includes the core SDK.

## Usage

```typescript
import { Appodeal, AppodealAdType } from 'capacitor-appodeal';

// Initialize
await Appodeal.initialize({
  appKey: 'YOUR_APP_KEY',
  adTypes: AppodealAdType.BANNER | AppodealAdType.INTERSTITIAL
});

// Show Banner
await Appodeal.showBanner();

// Hide Banner
await Appodeal.hideBanner();

// Show Interstitial
try {
  await Appodeal.showInterstitial();
} catch (e) {
  console.log('Interstitial not loaded');
}

// Listeners
Appodeal.addListener('onBannerLoaded', (info) => {
  console.log('Banner loaded', info);
});
```
