import { registerPlugin } from '@capacitor/core';

import type { AppodealPlugin } from './definitions';

const Appodeal = registerPlugin<AppodealPlugin>('Appodeal', {
    web: () => import('./web').then(m => new m.AppodealWeb()),
});

export * from './definitions';
export { Appodeal };
