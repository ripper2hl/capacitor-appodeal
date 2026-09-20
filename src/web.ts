import { WebPlugin } from '@capacitor/core';
import type { AppodealPlugin, BannerLoadedEvent, EmptyEvent, InterstitialLoadedEvent } from './definitions';

export class AppodealWeb extends WebPlugin implements AppodealPlugin {
    async initialize(options: { appKey: string; adTypes: number; testing?: boolean }): Promise<void> {
        console.log('AppodealWeb: initialize', options);
    }

    async showBanner(options?: { position?: number }): Promise<void> {
        console.log('AppodealWeb: showBanner', options);
    }

    async hideBanner(): Promise<void> {
        console.log('AppodealWeb: hideBanner');
    }

    async showInterstitial(): Promise<void> {
        console.log('AppodealWeb: showInterstitial');
    }

    // Satisfy TypeScript: web mock fires no events.
    // The real implementations are native-only.
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    async removeAllListeners(): Promise<void> {
        return super.removeAllListeners();
    }
}
