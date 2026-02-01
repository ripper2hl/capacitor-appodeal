# capacitor-appodeal

**Unofficial** Capacitor plugin for Appodeal Mediation Only integration. 

This plugin allows you to integrate Appodeal ads into your Capacitor application. It is designed for "Mediation Only" usage, meaning it provides the core SDK.

> **Disclaimer:** This project is not affiliated with, endorsed by, or connected to Appodeal. It is an open-source community plugin.

## Supported Platforms

| Platform | Supported | SDK Version | Notes |
| :--- | :---: | :---: | :--- |
| **Android** | ✅ | `3.12.0.1` | Native implementation |
| **Web** | ✅ | Mock | Development/Testing only |
| **iOS** | ❌ | - | Not implemented yet |

## Compatibility

| Plugin Version | Capacitor Version | Angular |
| :--- | :--- | :--- |
| `0.0.x` | `^6.0.0` | `16+` |

## Installation

### 1. Install NPM Package
```bash
npm install capacitor-appodeal
npx cap sync
```

### 2. Android Setup (CRITICAL)
Since the Appodeal SDK is hosted on a custom repository, you **MUST** add the repository URL to your project's `android/build.gradle` (usually the root one, inside `allprojects` or `repositories` block).

**File:** `android/build.gradle` (Project level)
```groovy
allprojects {
    repositories {
        google()
        mavenCentral()
        // ADD THIS LINE:
        maven { url "https://artifactory.appodeal.com/appodeal" }
    }
}
```

### 3. Network Security Config (Optional but Recommended)
Appodeal requires allowing cleartext traffic for some ad networks.
Create a file at `android/app/src/main/res/xml/network_security_config.xml` in your **main application** with the following content:

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

### 4. Update AndroidManifest.xml
Open your **main application's** `android/app/src/main/AndroidManifest.xml` and add the `networkSecurityConfig` attribute:

```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ... >
    <!-- ... -->
</application>
```

## Configuration

### Manual Ad Networks
By default, this plugin initializes the Appodeal SDK with the following adapters **excluded** to prevent tracking/compliance issues and size bloat:
- `adjust`
- `appsflyer`
- `facebook_analytics`
- `firebase` (This often includes AdMob/Google Ads components)

If you need these adapters, you must manually add them to your app's `build.gradle` dependencies.

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
