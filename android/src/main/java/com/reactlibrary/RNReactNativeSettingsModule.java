package com.reactlibrary;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import androidx.annotation.Nullable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.modules.appstate.AppStateModule;
import com.facebook.react.modules.core.DeviceEventManagerModule;

public class RNReactNativeSettingsModule extends ReactContextBaseJavaModule {
  private final ReactApplicationContext reactContext;
  private final AudioManager audioManager;
  private final VolumeChangeReceiver volumeChangeReceiver;
  private final NetworkChangeReceiver networkChangeReceiver;
  private static final long MIN_UPDATE_INTERVAL = 500; // Intervalo mínimo entre as notificações de mudança de volume (em milissegundos)
  private long lastVolumeUpdateTime = 0;

  private CameraManager cameraManager;
  private String cameraId;

  public RNReactNativeSettingsModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
    this.audioManager = (AudioManager) reactContext.getSystemService(Context.AUDIO_SERVICE);
    this.volumeChangeReceiver = new VolumeChangeReceiver(reactContext);
    this.networkChangeReceiver = new NetworkChangeReceiver(reactContext);

    cameraManager = (CameraManager) reactContext.getSystemService(Context.CAMERA_SERVICE);
    try {
        cameraId = cameraManager.getCameraIdList()[0];
    } catch (CameraAccessException e) {
        e.printStackTrace();
    }
  }

  @Override
  public String getName() {
    return "RNReactNativeSettings";
  }

  @Override
  public void onCatalystInstanceDestroy() {
    try {
      unregisterNetworkChangeReceiver(); // Desregistrar o receptor de transmissão antes de destruí-lo
      reactContext.unregisterReceiver(volumeChangeReceiver);
      reactContext.unregisterReceiver(networkChangeReceiver);
    } catch (Exception e) {
      // Lidar com exceção
      e.printStackTrace();
      // Emitir evento de erro para o JavaScript
      sendErrorEvent("Error", "Ocorreu um erro ao desregistrar os receptores de transmissão. Tente usar um módulo nativo.");
    }
  }

  private void unregisterNetworkChangeReceiver() {
    try {
      reactContext.unregisterReceiver(networkChangeReceiver);
    } catch (Exception e) {
      // Lidar com exceção
      e.printStackTrace();
      // Emitir evento de erro para o JavaScript
      sendErrorEvent("Error", "Ocorreu um erro ao desregistrar o receptor de transmissão. Tente usar um módulo nativo.");
    }
  }

  @ReactMethod
  public void getVolumeState(Callback callback) {
    try {
      if (isAppInForeground()) {
        if (isVolumeUpdateAllowed()) {
          int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
          int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

          WritableMap volumeState = Arguments.createMap();
          volumeState.putInt("currentVolume", currentVolume);
          volumeState.putInt("maxVolume", maxVolume);

          callback.invoke(volumeState);

          updateLastVolumeUpdateTime();
        }
      } else {
        // O aplicativo está em segundo plano, não faça nada
      }
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
      if (isAppInForeground()) {
        ConnectivityManager connectivityManager = (ConnectivityManager) reactContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        callback.invoke(isConnected);
      } else {
        // O aplicativo está em segundo plano, não faça nada
      }
    } catch (Exception e) {
      // Lidar com exceção
      e.printStackTrace();
      // Emitir evento de erro para o JavaScript
      sendErrorEvent("Error", "Ocorreu um erro ao verificar a conexão de internet. Tente usar um módulo nativo.");
    }
  }

  @Override
  public void initialize() {
    super.initialize();
    registerVolumeChangeReceiver();
    registerNetworkChangeReceiver();
  }

  private void registerVolumeChangeReceiver() {
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

  private boolean isAppInForeground() {
    String appState = reactContext.getCurrentActivity() != null ? AppStateModule.APP_STATE_ACTIVE : AppStateModule.APP_STATE_BACKGROUND;
    return appState.equals(AppStateModule.APP_STATE_ACTIVE);
  }

  private boolean isVolumeUpdateAllowed() {
    long currentTime = System.currentTimeMillis();
    return currentTime - lastVolumeUpdateTime > MIN_UPDATE_INTERVAL;
  }

  private void updateLastVolumeUpdateTime() {
    lastVolumeUpdateTime = System.currentTimeMillis();
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
        if (isVolumeUpdateAllowed()) {
          updateLastVolumeUpdateTime();

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
  }

  private class NetworkChangeReceiver extends BroadcastReceiver {
    private final ReactApplicationContext receiverContext;

    public NetworkChangeReceiver(ReactApplicationContext context) {
      this.receiverContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
        if (isAppInForeground()) {
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

    @ReactMethod
    public void turnOn() {
        try {
            cameraManager.setTorchMode(cameraId, true);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @ReactMethod
    public void turnOff() {
        try {
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
  
}
