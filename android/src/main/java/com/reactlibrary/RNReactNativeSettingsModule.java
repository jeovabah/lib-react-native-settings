package com.reactlibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.Nullable;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;

public class RNReactNativeSettingsModule extends ReactContextBaseJavaModule {
  private final ReactApplicationContext reactContext;
  private final AudioManager audioManager;
  private final VolumeChangeReceiver volumeChangeReceiver;
  private final NetworkChangeReceiver networkChangeReceiver;

  public RNReactNativeSettingsModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.audioManager = (AudioManager) reactContext.getSystemService(Context.AUDIO_SERVICE);
    this.volumeChangeReceiver = new VolumeChangeReceiver(reactContext);
    this.networkChangeReceiver = new NetworkChangeReceiver(reactContext);
  }

  @Override
  public String getName() {
    return "RNReactNativeSettings";
  }

  @Override
  public void onCatalystInstanceDestroy() {
    LocalBroadcastManager.getInstance(reactContext).unregisterReceiver(volumeChangeReceiver);
    reactContext.unregisterReceiver(networkChangeReceiver);
  }

  @ReactMethod
  public void getVolumeState(Callback callback) {
    int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

    WritableMap volumeState = Arguments.createMap();
    volumeState.putInt("currentVolume", currentVolume);
    volumeState.putInt("maxVolume", maxVolume);

    callback.invoke(volumeState);
  }

  @ReactMethod
  public void hasInternetConnection(Callback callback) {
    ConnectivityManager connectivityManager = (ConnectivityManager) reactContext.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
    boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    callback.invoke(isConnected);
  }

  @ReactMethod
  public void startListeningVolumeChanges() {
    IntentFilter filter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
    reactContext.registerReceiver(volumeChangeReceiver, filter);
  }

  @ReactMethod
  public void stopListeningVolumeChanges() {
    reactContext.unregisterReceiver(volumeChangeReceiver);
  }

  @ReactMethod
  public void startListeningNetworkChanges() {
    IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    reactContext.registerReceiver(networkChangeReceiver, filter);
  }

  @ReactMethod
  public void stopListeningNetworkChanges() {
    reactContext.unregisterReceiver(networkChangeReceiver);
  }

  private class VolumeChangeReceiver extends BroadcastReceiver {
    private final ReactApplicationContext receiverContext;

    public VolumeChangeReceiver(ReactApplicationContext context) {
      this.receiverContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        WritableMap volumeState = Arguments.createMap();
        volumeState.putInt("currentVolume", currentVolume);
        volumeState.putInt("maxVolume", maxVolume);

        sendEvent("VolumeChange", volumeState);
      }
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
      receiverContext
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit(eventName, params);
    }
  }

  private class NetworkChangeReceiver extends BroadcastReceiver {
    private final ReactApplicationContext receiverContext;

    public NetworkChangeReceiver(ReactApplicationContext context) {
      this.receiverContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
        ConnectivityManager connectivityManager = (ConnectivityManager) reactContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        WritableMap networkState = Arguments.createMap();
        networkState.putBoolean("isConnected", isConnected);

        sendEvent("NetworkChange", networkState);
      }
    }

    private void sendEvent(String eventName, @Nullable WritableMap params) {
      receiverContext
              .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
              .emit(eventName, params);
    }
  }
}
