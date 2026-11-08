import { WebPlugin } from '@capacitor/core';

import type { AppodealPlugin } from './definitions';

export class AppodealWeb extends WebPlugin implements AppodealPlugin {
  async initialize(options: {
    appKey: string;
    adTypes: number;
    testing?: boolean;
    disabledNetworks?: string[];
  }): Promise<void> {
    const safeOptions = {
      ...options,
      testing: options.testing === true,
    };
    console.log('AppodealWeb: initialize', safeOptions);
  }

  async disableNetwork(options: {
    network: string;
    adTypes?: number;
  }): Promise<void> {
    console.log('AppodealWeb: disableNetwork', options);
  }

  async getNetworks(options?: {
    adTypes?: number;
  }): Promise<{ networks: string[] }> {
    console.log('AppodealWeb: getNetworks', options);
    return { networks: [] };
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

  async showRewardedVideo(): Promise<void> {
    console.log('AppodealWeb: showRewardedVideo');
  }

  // Satisfy TypeScript: web mock fires no events.
  // The real implementations are native-only.
  async removeAllListeners(): Promise<void> {
    return super.removeAllListeners();
  }
}
