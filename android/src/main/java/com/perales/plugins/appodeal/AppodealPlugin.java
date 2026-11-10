package com.perales.plugins.appodeal;

import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.BannerCallbacks;
import com.appodeal.ads.InterstitialCallbacks;
import com.appodeal.ads.RewardedVideoCallbacks;
import com.appodeal.ads.initializing.ApdInitializationError;
import com.appodeal.ads.revenue.AdRevenueCallbacks;
import com.appodeal.ads.revenue.RevenueInfo;
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
public class AppodealPlugin extends Plugin implements BannerCallbacks, InterstitialCallbacks, RewardedVideoCallbacks, AdRevenueCallbacks {

    private static final String TAG = "AppodealPlugin";
    private static final String KEY_AD_TYPES = "adTypes";
    private static final String KEY_AD_TYPE = "adType";
    private static final String KEY_PLACEMENT = "placement";
    private static final String KEY_IS_PRECACHE = "isPrecache";
    private static final String ERROR_ACTIVITY_UNAVAILABLE = "Activity is not available";
    private static final String ERROR_MISSING_AD_TYPE = "Must provide adType";

    @FunctionalInterface
    private interface ActivityAction {
        void execute(AppCompatActivity activity);
    }

    private void runOnActivityUiThread(final PluginCall call, final ActivityAction action) {
        final AppCompatActivity activity = getActivity();
        if (activity == null) {
            call.reject(ERROR_ACTIVITY_UNAVAILABLE);
            return;
        }
        activity.runOnUiThread(() -> action.execute(activity));
    }

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

        runOnActivityUiThread(call, (activity) -> {
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
            Appodeal.setRewardedVideoCallbacks(AppodealPlugin.this);
            Appodeal.setAdRevenueCallbacks(AppodealPlugin.this);

            Appodeal.initialize(activity, appKey, adTypes, (List<ApdInitializationError> errors) -> {
                if (errors != null && !errors.isEmpty()) {
                    for (ApdInitializationError error : errors) {
                        Log.w(TAG, "Appodeal initialization warning: " + error.getMessage());
                    }
                }
                call.resolve(new JSObject());
            });
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

        runOnActivityUiThread(call, (activity) -> {
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
        runOnActivityUiThread(call, (activity) -> {
            List<String> networks = adTypes != null ? Appodeal.getNetworks(adTypes) : Appodeal.getNetworks();
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
    public void isLoaded(final PluginCall call) {
        final Integer adType = call.getInt(KEY_AD_TYPE);
        if (adType == null) {
            call.reject(ERROR_MISSING_AD_TYPE);
            return;
        }
        runOnActivityUiThread(call, (activity) -> {
            JSObject ret = new JSObject();
            ret.put("isLoaded", Appodeal.isLoaded(adType));
            call.resolve(ret);
        });
    }

    @PluginMethod
    public void canShow(final PluginCall call) {
        final Integer adType = call.getInt(KEY_AD_TYPE);
        final String placement = call.getString(KEY_PLACEMENT);
        if (adType == null) {
            call.reject(ERROR_MISSING_AD_TYPE);
            return;
        }
        runOnActivityUiThread(call, (activity) -> {
            boolean allowed = hasPlacement(placement) ? Appodeal.canShow(adType, placement.trim()) : Appodeal.canShow(adType);
            JSObject ret = new JSObject();
            ret.put("canShow", allowed);
            call.resolve(ret);
        });
    }

    @PluginMethod
    public void showBanner(final PluginCall call) {
        final Integer posOpt = call.getInt("position", Appodeal.BANNER_BOTTOM);
        final int position = posOpt != null ? posOpt : Appodeal.BANNER_BOTTOM;
        final String placement = call.getString(KEY_PLACEMENT);
        runOnActivityUiThread(call, (activity) -> {
            showAd(activity, position, placement);
            call.resolve();
        });
    }

    @PluginMethod
    public void hideBanner(final PluginCall call) {
        runOnActivityUiThread(call, (activity) -> {
            Appodeal.hide(activity, Appodeal.BANNER);
            call.resolve();
        });
    }

    @PluginMethod
    public void showInterstitial(final PluginCall call) {
        final String placement = call.getString(KEY_PLACEMENT);
        runOnActivityUiThread(call, (activity) -> {
            if (Appodeal.isLoaded(Appodeal.INTERSTITIAL)) {
                showAd(activity, Appodeal.INTERSTITIAL, placement);
                call.resolve();
            } else {
                call.reject("Interstitial not loaded");
            }
        });
    }

    @PluginMethod
    public void showRewardedVideo(final PluginCall call) {
        final String placement = call.getString(KEY_PLACEMENT);
        runOnActivityUiThread(call, (activity) -> {
            if (Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)) {
                showAd(activity, Appodeal.REWARDED_VIDEO, placement);
                call.resolve();
            } else {
                call.reject("Rewarded video not loaded");
            }
        });
    }

    private static boolean hasPlacement(final String placement) {
        return placement != null && !placement.trim().isEmpty();
    }

    private static void showAd(final AppCompatActivity activity, final int adType, final String placement) {
        if (hasPlacement(placement)) {
            Appodeal.show(activity, adType, placement.trim());
        } else {
            Appodeal.show(activity, adType);
        }
    }

    private static String safeString(final String value) {
        return value != null ? value : "";
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

    // RewardedVideoCallbacks
    @Override
    public void onRewardedVideoLoaded(boolean isPrecache) {
        JSObject ret = new JSObject();
        ret.put(KEY_IS_PRECACHE, isPrecache);
        notifyListeners("onRewardedVideoLoaded", ret);
    }

    @Override
    public void onRewardedVideoFailedToLoad() {
        notifyListeners("onRewardedVideoFailedToLoad", new JSObject());
    }

    @Override
    public void onRewardedVideoShown() {
        notifyListeners("onRewardedVideoShown", new JSObject());
    }

    @Override
    public void onRewardedVideoShowFailed() {
        notifyListeners("onRewardedVideoShowFailed", new JSObject());
    }

    @Override
    public void onRewardedVideoClicked() {
        notifyListeners("onRewardedVideoClicked", new JSObject());
    }

    @Override
    public void onRewardedVideoFinished(double amount, String name) {
        JSObject ret = new JSObject();
        ret.put("amount", amount);
        ret.put("currency", name != null ? name : "");
        notifyListeners("onRewardedVideoFinished", ret);
    }

    @Override
    public void onRewardedVideoClosed(boolean finished) {
        JSObject ret = new JSObject();
        ret.put("isFinished", finished);
        notifyListeners("onRewardedVideoClosed", ret);
    }

    @Override
    public void onRewardedVideoExpired() {
        // Appodeal SDK automatically handles refreshing expired rewarded video ads.
    }

    // AdRevenueCallbacks
    @Override
    public void onAdRevenueReceive(final RevenueInfo revenueInfo) {
        if (revenueInfo == null) {
            return;
        }
        JSObject ret = new JSObject();
        ret.put(KEY_AD_TYPE, revenueInfo.getAdType());
        ret.put("adTypeString", safeString(revenueInfo.getAdTypeString()));
        ret.put("networkName", safeString(revenueInfo.getNetworkName()));
        ret.put("demandSource", safeString(revenueInfo.getDemandSource()));
        ret.put("adUnitName", safeString(revenueInfo.getAdUnitName()));
        ret.put(KEY_PLACEMENT, safeString(revenueInfo.getPlacement()));
        ret.put("revenue", revenueInfo.getRevenue());
        ret.put("currency", safeString(revenueInfo.getCurrency()));
        ret.put("precision", safeString(revenueInfo.getRevenuePrecision()));
        notifyListeners("onAdRevenueReceive", ret);
    }
}
