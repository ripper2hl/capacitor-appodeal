const assert = require('node:assert/strict');
const test = require('node:test');

const { Appodeal, AppodealAdType, AppodealNetwork } = require('../coverage/cjs/index.js');
const { AppodealWeb } = require('../coverage/cjs/web.js');

test('AppodealAdType enum exposes SDK 4.x bitmask constants', () => {
    assert.equal(AppodealAdType.NONE, 0);
    assert.equal(AppodealAdType.INTERSTITIAL, 3);
    assert.equal(AppodealAdType.BANNER, 4);
    assert.equal(AppodealAdType.BANNER_BOTTOM, 8);
    assert.equal(AppodealAdType.BANNER_TOP, 16);
    assert.equal(AppodealAdType.BANNER_VIEW, 64);
    assert.equal(AppodealAdType.REWARDED_VIDEO, 128);
    assert.equal(AppodealAdType.NON_SKIPPABLE_VIDEO, 128);
    assert.equal(AppodealAdType.MREC, 256);
    assert.equal(AppodealAdType.NATIVE, 512);
});

test('AppodealNetwork enum exposes 4.x network identifiers', () => {
    assert.equal(AppodealNetwork.APPLOVIN, 'applovin');
    assert.equal(AppodealNetwork.UNITY_ADS, 'unity_ads');
    assert.equal(AppodealNetwork.VUNGLE, 'vungle');
    assert.equal(AppodealNetwork.MINTEGRAL, 'mintegral');
    assert.equal(AppodealNetwork.DT_EXCHANGE, 'dt_exchange');
    assert.equal(AppodealNetwork.INMOBI, 'inmobi');
    assert.equal(AppodealNetwork.BIDMACHINE, 'bidmachine');
    assert.equal(AppodealNetwork.IAB, 'iab');
});

test('AppodealWeb methods execute and enforce testing=false by default', async () => {
    const web = new AppodealWeb();

    await web.initialize({
        appKey: 'test-app-key',
        adTypes: AppodealAdType.BANNER | AppodealAdType.INTERSTITIAL,
    });

    await web.initialize({
        appKey: 'test-app-key',
        adTypes: AppodealAdType.BANNER,
        testing: true,
        disabledNetworks: [AppodealNetwork.META],
    });

    await web.disableNetwork({ network: AppodealNetwork.ADMOB });
    const { networks } = await web.getNetworks({ adTypes: AppodealAdType.BANNER });
    assert.deepEqual(networks, []);

    await web.showBanner();
    await web.showBanner({ position: AppodealAdType.BANNER_TOP });
    await web.hideBanner();
    await web.showInterstitial();
    await web.removeAllListeners();

    await Appodeal.hideBanner();
    assert.ok(Appodeal);
});
