package com.perales.plugins.appodeal;

import android.util.Log;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.InterstitialCallbacks;
import com.appodeal.ads.initializing.ApdInitializationError;
import com.appodeal.ads.utils.Log.LogLevel;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;

@CapacitorPlugin(name = "Appodeal")
public class AppodealPlugin extends Plugin implements BannerCallbacks, InterstitialCallbacks {

    private static final String TAG = "AppodealPlugin";
    private static final String KEY_AD_TYPES = "adTypes";
    private static final String KEY_IS_PRECACHE = "isPrecache";

    @PluginMethod
    public void initialize(final PluginCall call) {
        final String appKey = call.getString("appKey");
        final Integer adTypes = call.getInt(KEY_AD_TYPES);
        final boolean testing = Boolean.TRUE.equals(call.getBoolean("testing", false));
        final List<String> disabledNetworks = extractStringList(call.getArray("disabledNetworks"));

        if (appKey == null || appKey.trim().isEmpty() || adTypes == null) {
            call.reject("Must provide appKey and adTypes");
            return;
        }

        getActivity()
            .runOnUiThread(() -> {
                for (String network : disabledNetworks) {
                    Appodeal.disableNetwork(network);
                }

                Appodeal.setTesting(testing);
                if (testing) {
                    Log.w(TAG, "Appodeal initialized with testing=true. Ensure testing is disabled in production builds.");
                    Appodeal.setLogLevel(LogLevel.verbose);
                } else {
                    Appodeal.setLogLevel(LogLevel.none);
                }

                Appodeal.setBannerCallbacks(AppodealPlugin.this);
                Appodeal.setInterstitialCallbacks(AppodealPlugin.this);

                Appodeal.initialize(
                    getActivity(),
                    appKey,
                    adTypes,
                    (List<ApdInitializationError> errors) -> {
                        if (errors != null && !errors.isEmpty()) {
                            for (ApdInitializationError error : errors) {
                                Log.w(TAG, "Appodeal initialization warning: " + error.getMessage());
                            }
                        }
                        call.resolve(new JSObject());
                    }
                );
            });
    }

    @PluginMethod
    public void disableNetwork(final PluginCall call) {
        final String network = call.getString("network");
        final Integer adTypes = call.getInt(KEY_AD_TYPES);

        if (network == null || network.trim().isEmpty()) {
            call.reject("Must provide network name");
            return;
        }

        getActivity()
            .runOnUiThread(() -> {
                if (adTypes != null) {
                    Appodeal.disableNetwork(network.trim(), adTypes);
                } else {
                    Appodeal.disableNetwork(network.trim());
                }
                call.resolve();
            });
    }

    @PluginMethod
    public void getNetworks(final PluginCall call) {
        final Integer adTypes = call.getInt(KEY_AD_TYPES);
        getActivity()
            .runOnUiThread(() -> {
                List<String> networks = (adTypes != null) ? Appodeal.getNetworks(adTypes) : Appodeal.getNetworks();
                JSArray array = new JSArray();
                if (networks != null) {
                    for (String net : networks) {
                        array.put(net);
                    }
                }
                JSObject ret = new JSObject();
                ret.put("networks", array);
                call.resolve(ret);
            });
    }

    @PluginMethod
    public void showBanner(final PluginCall call) {
        final Integer posOpt = call.getInt("position", Appodeal.BANNER_BOTTOM);
        final int position = (posOpt != null) ? posOpt : Appodeal.BANNER_BOTTOM;
        getActivity()
            .runOnUiThread(() -> {
                Appodeal.show(getActivity(), position);
                call.resolve();
            });
    }

    @PluginMethod
    public void hideBanner(final PluginCall call) {
        getActivity()
            .runOnUiThread(() -> {
                Appodeal.hide(getActivity(), Appodeal.BANNER);
                call.resolve();
            });
    }

    @PluginMethod
    public void showInterstitial(final PluginCall call) {
        getActivity()
            .runOnUiThread(() -> {
                if (Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
                    Appodeal.show(getActivity(), Appodeal.INTERSTITIAL);
                    call.resolve();
                } else {
                    call.reject("Interstitial not loaded");
                }
            });
    }

    private static List<String> extractStringList(final JSArray array) {
        if (array == null || array.length() == 0) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            try {
                String value = array.getString(i);
                if (value != null && !value.trim().isEmpty()) {
                    result.add(value.trim());
                }
            } catch (JSONException ignored) {
                // Skip non-string elements
            }
        }
        return result;
    }

    // BannerCallbacks
    @Override
    public void onBannerLoaded(int height, boolean isPrecache) {
        JSObject ret = new JSObject();
        ret.put("height", height);
        ret.put(KEY_IS_PRECACHE, isPrecache);
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
        // Appodeal SDK automatically handles refreshing expired banner ads.
    }

    // InterstitialCallbacks
    @Override
    public void onInterstitialLoaded(boolean isPrecache) {
        JSObject ret = new JSObject();
        ret.put(KEY_IS_PRECACHE, isPrecache);
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
        // Appodeal SDK automatically handles refreshing expired interstitial ads.
    }
}
