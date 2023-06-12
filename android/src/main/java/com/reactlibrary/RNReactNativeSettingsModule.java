package com.reactlibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.core.DeviceEventManagerModule;

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
    try {
      registerNetworkChangeReceiver(); // Registrar o receptor de transmissão antes de desregistrá-lo
      LocalBroadcastManager.getInstance(reactContext).unregisterReceiver(volumeChangeReceiver);
      reactContext.unregisterReceiver(networkChangeReceiver);
    } catch (Exception e) {
      // Lidar com exceção
      e.printStackTrace();
      // Emitir evento de erro para o JavaScript
      sendErrorEvent("Error", "Ocorreu um erro ao desregistrar o receptor de transmissão. Tente usar um módulo nativo.");
    }
  }

  private void registerNetworkChangeReceiver() {
    try {
      IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
      reactContext.registerReceiver(networkChangeReceiver, filter);
    } catch (Exception e) {
      // Lidar com exceção
      e.printStackTrace();
      // Emitir evento de erro para o JavaScript
      sendErrorEvent("Error", "Ocorreu um erro ao registrar o receptor de transmissão. Tente usar um módulo nativo.");
    }
  }

  @ReactMethod
  public void getVolumeState(Callback callback) {
    try {
      int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
      int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

      WritableMap volumeState = Arguments.createMap();
      volumeState.putInt("currentVolume", currentVolume);
      volumeState.putInt("maxVolume", maxVolume);

      callback.invoke(volumeState);
    } catch (Exception e) {
      // Lidar com exceção
      e.printStackTrace();
      // Emitir evento de erro para o JavaScript
      sendErrorEvent("Error", "Ocorreu um erro ao obter o estado do volume. Tente usar um módulo nativo.");
    }
  }

  @ReactMethod
  public void hasInternetConnection(Callback callback) {
    try {
      ConnectivityManager connectivityManager = (ConnectivityManager) reactContext.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
      boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

      callback.invoke(isConnected);
    } catch (Exception e) {
      // Lidar com exceção
      e.printStackTrace();
      // Emitir evento de erro para o JavaScript
      sendErrorEvent("Error", "Ocorreu um erro ao verificar a conexão de internet. Tente usar um módulo nativo.");
    }
  }

  @ReactMethod
  public void startListeningVolumeChanges() {
    try {
      IntentFilter filter = new IntentFilter("android.media.VOLUME_CHANGED_ACTION");
      reactContext.registerReceiver(volumeChangeReceiver, filter);
    } catch (Exception e) {
      // Lidar com exceção
      e.printStackTrace();
      // Emitir evento de erro para o JavaScript
      sendErrorEvent("Error", "Ocorreu um erro ao iniciar a escuta das mudanças de volume. Tente usar um módulo nativo.");
    }
  }

  @ReactMethod
  public void stopListeningVolumeChanges() {
    try {
      reactContext.unregisterReceiver(volumeChangeReceiver);
    } catch (Exception e) {
      // Lidar com exceção
      e.printStackTrace();
      // Emitir evento de erro para o JavaScript
      sendErrorEvent("Error", "Ocorreu um erro ao parar a escuta das mudanças de volume. Tente usar um módulo nativo.");
    }
  }

  @ReactMethod
  public void startListeningNetworkChanges() {
    try {
      registerNetworkChangeReceiver(); // Registrar o receptor de transmissão antes de usá-lo
    } catch (Exception e) {
      // Lidar com exceção
      e.printStackTrace();
      // Emitir evento de erro para o JavaScript
      sendErrorEvent("Error", "Ocorreu um erro ao iniciar a escuta das mudanças de rede. Tente usar um módulo nativo.");
    }
  }

  @ReactMethod
  public void stopListeningNetworkChanges() {
    try {
      reactContext.unregisterReceiver(networkChangeReceiver);
    } catch (Exception e) {
      // Lidar com exceção
      e.printStackTrace();
      // Emitir evento de erro para o JavaScript
      sendErrorEvent("Error", "Ocorreu um erro ao parar a escuta das mudanças de rede. Tente usar um módulo nativo.");
    }
  }

  private void sendEvent(String eventName, @Nullable WritableMap params) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
      .emit(eventName, params);
  }

  private void sendErrorEvent(String eventName, String errorMessage) {
    WritableMap errorMap = Arguments.createMap();
    errorMap.putString("error", errorMessage);
    sendEvent(eventName, errorMap);
  }

  private class VolumeChangeReceiver extends BroadcastReceiver {
    private final ReactApplicationContext receiverContext;

    public VolumeChangeReceiver(ReactApplicationContext context) {
      this.receiverContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals("android.media.VOLUME_CHANGED_ACTION")) {
        try {
          int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
          int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

          WritableMap volumeState = Arguments.createMap();
          volumeState.putInt("currentVolume", currentVolume);
          volumeState.putInt("maxVolume", maxVolume);

          sendEvent("VolumeChange", volumeState);
        } catch (Exception e) {
          // Lidar com exceção
          e.printStackTrace();
          // Emitir evento de erro para o JavaScript
          sendErrorEvent("Error", "Ocorreu um erro ao obter as mudanças de volume. Tente usar um módulo nativo.");
        }
      }
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
        try {
          ConnectivityManager connectivityManager = (ConnectivityManager) reactContext.getSystemService(Context.CONNECTIVITY_SERVICE);
          NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
          boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

          WritableMap networkState = Arguments.createMap();
          networkState.putBoolean("isConnected", isConnected);

          sendEvent("NetworkChange", networkState);
        } catch (Exception e) {
          // Lidar com exceção
          e.printStackTrace();
          // Emitir evento de erro para o JavaScript
          sendErrorEvent("Error", "Ocorreu um erro ao obter as mudanças de rede. Tente usar um módulo nativo.");
        }
      }
    }
  }
}
