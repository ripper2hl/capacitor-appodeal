import type { PluginListenerHandle } from '@capacitor/core';

// ---------------------------------------------------------------------------
// Ad type flags — match Appodeal SDK 4.x constants.
// Note: NON_SKIPPABLE_VIDEO shares value 128 with REWARDED_VIDEO for
// backwards compatibility.
// ---------------------------------------------------------------------------
export enum AppodealAdType {
  NONE = 0,
  INTERSTITIAL = 3,
  BANNER = 4,
  BANNER_BOTTOM = 8,
  BANNER_TOP = 16,
  BANNER_VIEW = 64,
  REWARDED_VIDEO = 128,
  NON_SKIPPABLE_VIDEO = 128,
  MREC = 256,
  NATIVE = 512,
}

// ---------------------------------------------------------------------------
// Ad network identifiers — match Appodeal SDK 4.x adapter names.
// Can be used with `disableNetwork()` or `initialize({ disabledNetworks })`.
// ---------------------------------------------------------------------------
export enum AppodealNetwork {
  ADMOB = 'admob',
  ADMOB_NEXT_GEN = 'admob_next_gen',
  AMAZON = 'amazon',
  APPLOVIN = 'applovin',
  APPLOVIN_MAX = 'applovin_max',
  BIDMACHINE = 'bidmachine',
  BIDON = 'bidon',
  BIGO_ADS = 'bigo_ads',
  CHARTBOOST = 'chartboost',
  DT_EXCHANGE = 'dt_exchange',
  IAB = 'iab',
  INMOBI = 'inmobi',
  IRONSOURCE = 'ironsource',
  LEVEL_PLAY = 'level_play',
  META = 'meta',
  MINTEGRAL = 'mintegral',
  MOBILEFUSE = 'mobilefuse',
  MOLOCO = 'moloco',
  MY_TARGET = 'my_target',
  OGURY = 'ogury',
  PANGLE = 'pangle',
  PUBMATIC = 'pubmatic',
  SMAATO = 'smaato',
  STARTIO = 'startio',
  TAURUSX = 'taurusx',
  UNITY_ADS = 'unity_ads',
  VERVE = 'verve',
  VUNGLE = 'vungle',
  YANDEX = 'yandex',
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
   * @param options.appKey            Your Appodeal application key.
   * @param options.adTypes           Bitwise OR of `AppodealAdType` values.
   * @param options.testing           Set to `true` to show test ads. Default: `false`.
   *                                  Never leave `true` in production builds.
   * @param options.disabledNetworks  Optional list of network names to disable
   *                                  before initialization.
   */
  initialize(options: {
    appKey: string;
    adTypes: number;
    testing?: boolean;
    disabledNetworks?: string[];
  }): Promise<void>;

  /**
   * Disable a specific ad network before calling `initialize()`.
   *
   * @param options.network  Network identifier (e.g. `AppodealNetwork.META` or `'meta'`).
   * @param options.adTypes  Optional bitwise OR of `AppodealAdType` values to disable
   *                         the network only for specific formats. If omitted, disables
   *                         the network for all ad types.
   */
  disableNetwork(options: { network: string; adTypes?: number }): Promise<void>;

  /**
   * Return the list of registered ad networks in the current build.
   *
   * @param options.adTypes  Optional bitwise OR of `AppodealAdType` values to filter by format.
   */
  getNetworks(options?: { adTypes?: number }): Promise<{ networks: string[] }>;

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
  addListener(
    eventName: string,
    listenerFunc: (...args: any[]) => any,
  ): Promise<PluginListenerHandle>;

  /** Remove all registered listeners at once. */
  removeAllListeners(): Promise<void>;
}
