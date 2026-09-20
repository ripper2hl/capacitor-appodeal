import type { PluginListenerHandle } from '@capacitor/core';

// ---------------------------------------------------------------------------
// Ad type flags — match Appodeal SDK constants.
// Note: REWARDED_VIDEO and NON_SKIPPABLE_VIDEO share value 128 intentionally;
// the Appodeal SDK treats them the same.
// ---------------------------------------------------------------------------
export enum AppodealAdType {
    INTERSTITIAL = 3,
    BANNER = 4,
    BANNER_BOTTOM = 8,
    BANNER_TOP = 16,
    REWARDED_VIDEO = 128,
    NON_SKIPPABLE_VIDEO = 128,
    MREC = 256,
    NATIVE = 512,
}

// ---------------------------------------------------------------------------
// Event payload types
// ---------------------------------------------------------------------------

export interface BannerLoadedEvent {
    /** Height of the loaded banner in pixels. */
    height: number;
    /** Whether this is a pre-cached ad. */
    isPrecache: boolean;
}

export interface InterstitialLoadedEvent {
    /** Whether this is a pre-cached ad. */
    isPrecache: boolean;
}

/** Generic empty payload for events that carry no data. */
export type EmptyEvent = Record<string, never>;

// ---------------------------------------------------------------------------
// Plugin interface
// ---------------------------------------------------------------------------

export interface AppodealPlugin {
    /**
     * Initialize the Appodeal SDK.
     * Must be called before showing any ads.
     *
     * @param options.appKey   Your Appodeal application key.
     * @param options.adTypes  Bitwise OR of `AppodealAdType` values.
     * @param options.testing  Set to `true` to show test ads. Default: `false`.
     */
    initialize(options: { appKey: string; adTypes: number; testing?: boolean }): Promise<void>;

    /**
     * Show a banner ad.
     *
     * @param options.position  Use `AppodealAdType.BANNER_BOTTOM` (8) or
     *                          `AppodealAdType.BANNER_TOP` (16).
     *                          Defaults to `BANNER_BOTTOM` if omitted.
     */
    showBanner(options?: { position?: number }): Promise<void>;

    /** Hide the currently shown banner. */
    hideBanner(): Promise<void>;

    /**
     * Show an interstitial ad.
     * Rejects if the interstitial is not loaded yet — listen to
     * `onInterstitialLoaded` before calling this.
     */
    showInterstitial(): Promise<void>;

    // ── Banner listeners ────────────────────────────────────────────────────

    addListener(
        eventName: 'onBannerLoaded',
        listenerFunc: (event: BannerLoadedEvent) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onBannerFailedToLoad',
        listenerFunc: (event: EmptyEvent) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onBannerShown',
        listenerFunc: (event: EmptyEvent) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onBannerClicked',
        listenerFunc: (event: EmptyEvent) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onBannerShowFailed',
        listenerFunc: (event: EmptyEvent) => void,
    ): Promise<PluginListenerHandle>;

    // ── Interstitial listeners ───────────────────────────────────────────────

    addListener(
        eventName: 'onInterstitialLoaded',
        listenerFunc: (event: InterstitialLoadedEvent) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onInterstitialFailedToLoad',
        listenerFunc: (event: EmptyEvent) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onInterstitialShown',
        listenerFunc: (event: EmptyEvent) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onInterstitialShowFailed',
        listenerFunc: (event: EmptyEvent) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onInterstitialClicked',
        listenerFunc: (event: EmptyEvent) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onInterstitialClosed',
        listenerFunc: (event: EmptyEvent) => void,
    ): Promise<PluginListenerHandle>;

    /** Catch-all overload for custom event names. */
    addListener(eventName: string, listenerFunc: (...args: any[]) => any): Promise<PluginListenerHandle>;

    /** Remove all registered listeners at once. */
    removeAllListeners(): Promise<void>;
}
