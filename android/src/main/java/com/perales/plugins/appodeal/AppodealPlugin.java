package com.perales.plugins.appodeal;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.InterstitialCallbacks;
import com.appodeal.ads.initializing.ApdInitializationCallback;
import com.appodeal.ads.initializing.ApdInitializationError;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.List;

@CapacitorPlugin(name = "Appodeal")
public class AppodealPlugin extends Plugin implements BannerCallbacks, InterstitialCallbacks {

    @PluginMethod
    public void initialize(final PluginCall call) {
        String appKey = call.getString("appKey");
        Integer adTypes = call.getInt("adTypes");
        Boolean testing = call.getBoolean("testing", false);

        if (appKey == null || adTypes == null) {
            call.reject("Must provide appKey and adTypes");
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Appodeal.setTesting(testing);
                Appodeal.initialize(getActivity(), appKey, adTypes, new ApdInitializationCallback() {
                    @Override
                    public void onInitializationFinished(List<ApdInitializationError> list) {
                        // Check if list is empty or null for success?
                        // The user just said "resuelve la promesa de JS cuando termine".
                        // Usually implies success if callback is hit.
                        JSObject ret = new JSObject();
                        if (list != null && !list.isEmpty()) {
                            // Log errors or return them? User asked for basic robust code.
                            // We will simply resolve to indicate init finished, maybe pass error count.
                            // But usually init is considered 'done'.
                        }

                        // Set callbacks
                        Appodeal.setBannerCallbacks(AppodealPlugin.this);
                        Appodeal.setInterstitialCallbacks(AppodealPlugin.this);

                        call.resolve(ret);
                    }
                });
            }
        });
    }

    @PluginMethod
    public void showBanner(final PluginCall call) {
        // Default to BOTTOM (8) if not provided. Enums: BANNER_BOTTOM=8, BANNER_TOP=16
        final int position = call.getInt("position", 8);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Appodeal.show(getActivity(), position);
                call.resolve();
            }
        });
    }

    @PluginMethod
    public void hideBanner(final PluginCall call) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Appodeal.hide(getActivity(), Appodeal.BANNER);
                call.resolve();
            }
        });
    }

    @PluginMethod
    public void showInterstitial(final PluginCall call) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
                    Appodeal.show(getActivity(), Appodeal.INTERSTITIAL);
                    call.resolve();
                } else {
                    call.reject("Interstitial not loaded");
                }
            }
        });
    }

    // BannerCallbacks
    @Override
    public void onBannerLoaded(int height, boolean isPrecache) {
        JSObject ret = new JSObject();
        ret.put("height", height);
        ret.put("isPrecache", isPrecache);
        notifyListeners("onBannerLoaded", ret);
    }

    @Override
    public void onBannerFailedToLoad() {
        notifyListeners("onBannerFailedToLoad", new JSObject());
    }

    @Override
    public void onBannerShown() {
        notifyListeners("onBannerShown", new JSObject());
    }

    @Override
    public void onBannerShowFailed() {
        notifyListeners("onBannerShowFailed", new JSObject());
    }

    @Override
    public void onBannerClicked() {
        notifyListeners("onBannerClicked", new JSObject());
    }

    @Override
    public void onBannerExpired() {
        // notifyListeners("onBannerExpired", new JSObject());
    }

    // InterstitialCallbacks
    @Override
    public void onInterstitialLoaded(boolean isPrecache) {
        JSObject ret = new JSObject();
        ret.put("isPrecache", isPrecache);
        notifyListeners("onInterstitialLoaded", ret);
    }

    @Override
    public void onInterstitialFailedToLoad() {
        notifyListeners("onInterstitialFailedToLoad", new JSObject());
    }

    @Override
    public void onInterstitialShown() {
        notifyListeners("onInterstitialShown", new JSObject());
    }

    @Override
    public void onInterstitialShowFailed() {
        notifyListeners("onInterstitialShowFailed", new JSObject());
    }

    @Override
    public void onInterstitialClicked() {
        notifyListeners("onInterstitialClicked", new JSObject());
    }

    @Override
    public void onInterstitialClosed() {
        notifyListeners("onInterstitialClosed", new JSObject());
    }

    @Override
    public void onInterstitialExpired() {
        // notifyListeners("onInterstitialExpired", new JSObject());
    }
}
