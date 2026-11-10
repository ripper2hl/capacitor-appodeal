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

  async disableNetwork(options: { network: string; adTypes?: number }): Promise<void> {
    console.log('AppodealWeb: disableNetwork', options);
  }

  async getNetworks(options?: { adTypes?: number }): Promise<{ networks: string[] }> {
    console.log('AppodealWeb: getNetworks', options);
    return { networks: [] };
  }

  async isLoaded(options: { adType: number }): Promise<{ isLoaded: boolean }> {
    console.log('AppodealWeb: isLoaded', options);
    return { isLoaded: false };
  }

  async canShow(options: { adType: number; placement?: string }): Promise<{ canShow: boolean }> {
    console.log('AppodealWeb: canShow', options);
    return { canShow: false };
  }

  async showBanner(options?: { position?: number; placement?: string }): Promise<void> {
    console.log('AppodealWeb: showBanner', options);
  }

  async hideBanner(): Promise<void> {
    console.log('AppodealWeb: hideBanner');
  }

  async showInterstitial(options?: { placement?: string }): Promise<void> {
    console.log('AppodealWeb: showInterstitial', options);
  }

  async showRewardedVideo(options?: { placement?: string }): Promise<void> {
    console.log('AppodealWeb: showRewardedVideo', options);
  }

  // Satisfy TypeScript: web mock fires no events.
  // The real implementations are native-only.
  async removeAllListeners(): Promise<void> {
    return super.removeAllListeners();
  }
}
