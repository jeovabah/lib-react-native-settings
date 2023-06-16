
# react-native-react-native-settings

## Getting started

`$ npm install react-native-react-native-settings --save`
`$ yarn add react-native-react-native-settings`

#### Android
 ### Se usar expo, lembre de baixar um apk de desenvolvimento que use o npx expo start --dev-client. (Modulos nativos tais como esse, precisa ser rebuildado para funcionar no expo.)

## Usage, Version >= 1.0.14 or version more attualized
```javascript


import { useEffect } from "react";
import { DeviceEventEmitter } from "react-native";
import ReactNativeSettings from "react-native-react-native-settings";

    useEffect(() => {
      const volumeChangeListener = DeviceEventEmitter.addListener(
        "VolumeChange",
        (data) => {
          if (AppState.currentState === "active") {
            console.log("Volume mudou para", data?.currentVolume);
          }
        }
      );

      const listenerNetwork = DeviceEventEmitter.addListener(
          "NetworkChange",
          (data) => {
            console.log("internet: ", data);
          }
        );

      return () => {
        volumeChangeListener.remove();
        listenerNetwork.remove();
      };
    }, []);

```


## Usage, Version < 1.0.13
```javascript


import { useEffect } from "react";
import { DeviceEventEmitter } from "react-native";
import RNReactNativeSettings from "react-native-react-native-settings";


  useEffect(() => {
      // Voce precisa chamar o startListeningVolumeChanges() para começar a monitorar o volume
        RNReactNativeSettings.startListeningVolumeChanges();
        const listenerVolume = DeviceEventEmitter.addListener(
          "VolumeChange",
          (data) => {
            console.log("Volume mudou para", data.currentVolume);
          }
        );

      // Voce precisa chamar o startListeningNetworkChanges() para começar a monitorar a internet
        RNReactNativeSettings.startListeningNetworkChanges();
        const listenerNetwork = DeviceEventEmitter.addListener(
          "NetworkChange",
          (data) => {
            console.log("internet: ", data);
          }
        );

        return () => {
          listenerNetwork.remove();
          listenerVolume.remove();
        };
  }, []);
// TODO: What to do with the module?
RNReactNativeSettings;
```
  