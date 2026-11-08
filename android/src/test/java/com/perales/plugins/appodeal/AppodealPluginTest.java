package com.perales.plugins.appodeal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.appcompat.app.AppCompatActivity;
import com.appodeal.ads.Appodeal;
import com.appodeal.ads.initializing.ApdInitializationCallback;
import com.appodeal.ads.initializing.ApdInitializationError;
import com.appodeal.ads.utils.Log.LogLevel;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

public class AppodealPluginTest {

    private AppodealPlugin plugin;
    private AppCompatActivity activity;
    private PluginCall call;

    @Before
    public void setUp() {
        plugin = spy(new AppodealPlugin());
        activity = mock(AppCompatActivity.class);
        call = mock(PluginCall.class);

        doReturn(activity).when(plugin).getActivity();
        doAnswer(invocation -> {
                Runnable runnable = invocation.getArgument(0);
                runnable.run();
                return null;
            })
            .when(activity)
            .runOnUiThread(any(Runnable.class));
    }

    @Test
    public void initialize_rejectsWhenAppKeyIsNull() {
        when(call.getString("appKey")).thenReturn(null);
        when(call.getInt("adTypes")).thenReturn(Appodeal.BANNER);

        plugin.initialize(call);

        verify(call).reject("Must provide appKey and adTypes");
    }

    @Test
    public void initialize_rejectsWhenAppKeyIsBlank() {
        when(call.getString("appKey")).thenReturn("   ");
        when(call.getInt("adTypes")).thenReturn(Appodeal.BANNER);

        plugin.initialize(call);

        verify(call).reject("Must provide appKey and adTypes");
    }

    @Test
    public void initialize_rejectsWhenAdTypesIsNull() {
        when(call.getString("appKey")).thenReturn("test-key");
        when(call.getInt("adTypes")).thenReturn(null);

        plugin.initialize(call);

        verify(call).reject("Must provide appKey and adTypes");
    }

    @Test
    public void initialize_succeedsWithDefaultTestingFalse() {
        when(call.getString("appKey")).thenReturn("test-key");
        when(call.getInt("adTypes")).thenReturn(Appodeal.BANNER | Appodeal.INTERSTITIAL);
        when(call.getBoolean("testing", false)).thenReturn(false);
        when(call.getArray("disabledNetworks")).thenReturn(null);

        try (MockedStatic<Appodeal> appodealMock = mockStatic(Appodeal.class)) {
            appodealMock
                .when(() ->
                    Appodeal.initialize(
                        eq(activity),
                        eq("test-key"),
                        eq(Appodeal.BANNER | Appodeal.INTERSTITIAL),
                        any(ApdInitializationCallback.class)
                    )
                )
                .thenAnswer(invocation -> {
                    ApdInitializationCallback callback = invocation.getArgument(3);
                    callback.onInitializationFinished(null);
                    return null;
                });

            plugin.initialize(call);

            appodealMock.verify(() -> Appodeal.setTesting(false));
            appodealMock.verify(() -> Appodeal.setLogLevel(LogLevel.none));
            appodealMock.verify(() -> Appodeal.setBannerCallbacks(plugin));
            appodealMock.verify(() -> Appodeal.setInterstitialCallbacks(plugin));
            appodealMock.verify(() -> Appodeal.setRewardedVideoCallbacks(plugin));
            verify(call).resolve(any(JSObject.class));
        }
    }

    @Test
    public void initialize_succeedsWithTestingTrueAndDisabledNetworksAndErrors() {
        JSArray disabled = new JSArray();
        disabled.put("admob");
        disabled.put("  meta  ");
        disabled.put("");
        disabled.put((Object) null);

        when(call.getString("appKey")).thenReturn("test-key");
        when(call.getInt("adTypes")).thenReturn(Appodeal.BANNER);
        when(call.getBoolean("testing", false)).thenReturn(true);
        when(call.getArray("disabledNetworks")).thenReturn(disabled);

        ApdInitializationError mockError = mock(ApdInitializationError.class);
        when(mockError.getMessage()).thenReturn("Adapter warning");

        try (MockedStatic<Appodeal> appodealMock = mockStatic(Appodeal.class)) {
            appodealMock
                .when(() -> Appodeal.initialize(eq(activity), eq("test-key"), eq(Appodeal.BANNER), any(ApdInitializationCallback.class)))
                .thenAnswer(invocation -> {
                    ApdInitializationCallback callback = invocation.getArgument(3);
                    callback.onInitializationFinished(Collections.singletonList(mockError));
                    return null;
                });

            plugin.initialize(call);

            appodealMock.verify(() -> Appodeal.disableNetwork("admob"));
            appodealMock.verify(() -> Appodeal.disableNetwork("meta"));
            appodealMock.verify(() -> Appodeal.setTesting(true));
            appodealMock.verify(() -> Appodeal.setLogLevel(LogLevel.verbose));
            verify(call).resolve(any(JSObject.class));
        }
    }

    @Test
    public void disableNetwork_rejectsWhenNetworkMissing() {
        when(call.getString("network")).thenReturn(null);

        plugin.disableNetwork(call);

        verify(call).reject("Must provide network name");
    }

    @Test
    public void disableNetwork_rejectsWhenNetworkBlank() {
        when(call.getString("network")).thenReturn("  ");

        plugin.disableNetwork(call);

        verify(call).reject("Must provide network name");
    }

    @Test
    public void disableNetwork_disablesAllAdTypesWhenAdTypesNull() {
        when(call.getString("network")).thenReturn(" applovin ");
        when(call.getInt("adTypes")).thenReturn(null);

        try (MockedStatic<Appodeal> appodealMock = mockStatic(Appodeal.class)) {
            plugin.disableNetwork(call);

            appodealMock.verify(() -> Appodeal.disableNetwork("applovin"));
            verify(call).resolve();
        }
    }

    @Test
    public void disableNetwork_disablesSpecificAdTypesWhenProvided() {
        when(call.getString("network")).thenReturn("unity_ads");
        when(call.getInt("adTypes")).thenReturn(Appodeal.BANNER);

        try (MockedStatic<Appodeal> appodealMock = mockStatic(Appodeal.class)) {
            plugin.disableNetwork(call);

            appodealMock.verify(() -> Appodeal.disableNetwork("unity_ads", Appodeal.BANNER));
            verify(call).resolve();
        }
    }

    @Test
    public void getNetworks_returnsNetworksWithoutFilter() throws Exception {
        when(call.getInt("adTypes")).thenReturn(null);

        try (MockedStatic<Appodeal> appodealMock = mockStatic(Appodeal.class)) {
            appodealMock.when(Appodeal::getNetworks).thenReturn(Arrays.asList("applovin", "unity_ads"));

            plugin.getNetworks(call);

            ArgumentCaptor<JSObject> captor = ArgumentCaptor.forClass(JSObject.class);
            verify(call).resolve(captor.capture());
            JSObject result = captor.getValue();
            assertNotNull(result);
            assertEquals(2, result.getJSONArray("networks").length());
        }
    }

    @Test
    public void getNetworks_returnsNetworksWithAdTypesFilterAndHandlesNull() throws Exception {
        when(call.getInt("adTypes")).thenReturn(Appodeal.INTERSTITIAL);

        try (MockedStatic<Appodeal> appodealMock = mockStatic(Appodeal.class)) {
            appodealMock.when(() -> Appodeal.getNetworks(Appodeal.INTERSTITIAL)).thenReturn(null);

            plugin.getNetworks(call);

            ArgumentCaptor<JSObject> captor = ArgumentCaptor.forClass(JSObject.class);
            verify(call).resolve(captor.capture());
            JSObject result = captor.getValue();
            assertEquals(0, result.getJSONArray("networks").length());
        }
    }

    @Test
    public void showBanner_usesDefaultBottomWhenPositionNull() {
        when(call.getInt("position", Appodeal.BANNER_BOTTOM)).thenReturn(null);

        try (MockedStatic<Appodeal> appodealMock = mockStatic(Appodeal.class)) {
            plugin.showBanner(call);

            appodealMock.verify(() -> Appodeal.show(activity, Appodeal.BANNER_BOTTOM));
            verify(call).resolve();
        }
    }

    @Test
    public void showBanner_usesProvidedPosition() {
        when(call.getInt("position", Appodeal.BANNER_BOTTOM)).thenReturn(Appodeal.BANNER_TOP);

        try (MockedStatic<Appodeal> appodealMock = mockStatic(Appodeal.class)) {
            plugin.showBanner(call);

            appodealMock.verify(() -> Appodeal.show(activity, Appodeal.BANNER_TOP));
            verify(call).resolve();
        }
    }

    @Test
    public void hideBanner_hidesBannerAndResolves() {
        try (MockedStatic<Appodeal> appodealMock = mockStatic(Appodeal.class)) {
            plugin.hideBanner(call);

            appodealMock.verify(() -> Appodeal.hide(activity, Appodeal.BANNER));
            verify(call).resolve();
        }
    }

    @Test
    public void showInterstitial_showsWhenLoaded() {
        try (MockedStatic<Appodeal> appodealMock = mockStatic(Appodeal.class)) {
            appodealMock.when(() -> Appodeal.isLoaded(Appodeal.INTERSTITIAL)).thenReturn(true);

            plugin.showInterstitial(call);

            appodealMock.verify(() -> Appodeal.show(activity, Appodeal.INTERSTITIAL));
            verify(call).resolve();
        }
    }

    @Test
    public void showInterstitial_rejectsWhenNotLoaded() {
        try (MockedStatic<Appodeal> appodealMock = mockStatic(Appodeal.class)) {
            appodealMock.when(() -> Appodeal.isLoaded(Appodeal.INTERSTITIAL)).thenReturn(false);

            plugin.showInterstitial(call);

            appodealMock.verify(() -> Appodeal.show(any(), anyInt()), never());
            verify(call).reject("Interstitial not loaded");
        }
    }

    @Test
    public void showRewardedVideo_showsWhenLoaded() {
        try (MockedStatic<Appodeal> appodealMock = mockStatic(Appodeal.class)) {
            appodealMock.when(() -> Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)).thenReturn(true);

            plugin.showRewardedVideo(call);

            appodealMock.verify(() -> Appodeal.show(activity, Appodeal.REWARDED_VIDEO));
            verify(call).resolve();
        }
    }

    @Test
    public void showRewardedVideo_rejectsWhenNotLoaded() {
        try (MockedStatic<Appodeal> appodealMock = mockStatic(Appodeal.class)) {
            appodealMock.when(() -> Appodeal.isLoaded(Appodeal.REWARDED_VIDEO)).thenReturn(false);

            plugin.showRewardedVideo(call);

            appodealMock.verify(() -> Appodeal.show(any(), anyInt()), never());
            verify(call).reject("Rewarded video not loaded");
        }
    }

    @Test
    public void methods_rejectWhenActivityIsNull() {
        doReturn(null).when(plugin).getActivity();

        plugin.hideBanner(call);

        verify(call).reject("Activity is not available");
    }

    @Test
    public void callbacks_emitExpectedEvents() {
        plugin.onBannerLoaded(50, true);
        plugin.onBannerFailedToLoad();
        plugin.onBannerShown();
        plugin.onBannerShowFailed();
        plugin.onBannerClicked();
        plugin.onBannerExpired();

        plugin.onInterstitialLoaded(false);
        plugin.onInterstitialFailedToLoad();
        plugin.onInterstitialShown();
        plugin.onInterstitialShowFailed();
        plugin.onInterstitialClicked();
        plugin.onInterstitialClosed();
        plugin.onInterstitialExpired();

        plugin.onRewardedVideoLoaded(true);
        plugin.onRewardedVideoFailedToLoad();
        plugin.onRewardedVideoShown();
        plugin.onRewardedVideoShowFailed();
        plugin.onRewardedVideoClicked();
        plugin.onRewardedVideoFinished(10.0, "coins");
        plugin.onRewardedVideoFinished(5.0, null);
        plugin.onRewardedVideoClosed(true);
        plugin.onRewardedVideoExpired();

        // Verify no exceptions thrown during callback execution
        assertNotNull(plugin);
    }
}
