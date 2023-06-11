import { NativeModules } from "react-native";

const { RNReactNativeSettings } = NativeModules;

const ReactNativeSettings: RNReactNativeSettingsProps = RNReactNativeSettings;

interface RNReactNativeSettingsProps {
  getVolumeState: (
    callback: (state: { currentVolume: string; maxVolume: string }) => void
  ) => void;
  hasInternetConnection: (callback: (state: boolean) => void) => void;
  startListeningVolumeChanges: () => void;
  stopListeningVolumeChanges: () => void;
  startListeningNetworkChanges: () => void;
  stopListeningNetworkChanges: () => void;
}

export default ReactNativeSettings as RNReactNativeSettingsProps;
