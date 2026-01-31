import { WebPlugin } from '@capacitor/core';
import { AppodealPlugin } from './definitions';

export class AppodealWeb extends WebPlugin implements AppodealPlugin {
    async initialize(options: { appKey: string; adTypes: number }): Promise<void> {
        console.log('AppodealWeb: initialize', options);
    }

    async showBanner(position: number): Promise<void> {
        console.log('AppodealWeb: showBanner', position);
    }

    async hideBanner(): Promise<void> {
        console.log('AppodealWeb: hideBanner');
    }

    async showInterstitial(): Promise<void> {
        console.log('AppodealWeb: showInterstitial');
    }
}
