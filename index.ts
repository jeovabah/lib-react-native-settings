import { NativeModules } from "react-native";

const { RNReactNativeSettings } = NativeModules;

interface RNReactNativeSettingsProps {
  getVolumeState: () => void;
}

export default RNReactNativeSettings as RNReactNativeSettingsProps;
