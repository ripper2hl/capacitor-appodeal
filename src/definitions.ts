import type { PluginListenerHandle } from '@capacitor/core';

export enum AppodealAdType {
    INTERSTITIAL = 3,
    BANNER = 4,
    BANNER_BOTTOM = 8,
    BANNER_TOP = 16,
    REWARDED_VIDEO = 128,
    NON_SKIPPABLE_VIDEO = 128,
    MREC = 256,
    NATIVE = 512
}

export interface AppodealPlugin {
    initialize(options: { appKey: string; adTypes: number; testing?: boolean }): Promise<void>;
    showBanner(position: number): Promise<void>;
    hideBanner(): Promise<void>;
    showInterstitial(): Promise<void>;

    addListener(
        eventName: 'onBannerLoaded',
        listenerFunc: (info: any) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onBannerFailedToLoad',
        listenerFunc: (info: any) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onBannerShown',
        listenerFunc: (info: any) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onBannerClicked',
        listenerFunc: (info: any) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onBannerShowFailed',
        listenerFunc: (info: any) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onInterstitialLoaded',
        listenerFunc: (info: any) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onInterstitialFailedToLoad',
        listenerFunc: (info: any) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onInterstitialShown',
        listenerFunc: (info: any) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onInterstitialShowFailed',
        listenerFunc: (info: any) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onInterstitialClicked',
        listenerFunc: (info: any) => void,
    ): Promise<PluginListenerHandle>;

    addListener(
        eventName: 'onInterstitialClosed',
        listenerFunc: (info: any) => void,
    ): Promise<PluginListenerHandle>;

    addListener(eventName: string, listenerFunc: (...args: any[]) => any): Promise<PluginListenerHandle>;
}
