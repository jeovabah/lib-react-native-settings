using ReactNative.Bridge;
using System;
using System.Collections.Generic;
using Windows.ApplicationModel.Core;
using Windows.UI.Core;

namespace React.Native.Settings.RNReactNativeSettings
{
    /// <summary>
    /// A module that allows JS to share data.
    /// </summary>
    class RNReactNativeSettingsModule : NativeModuleBase
    {
        /// <summary>
        /// Instantiates the <see cref="RNReactNativeSettingsModule"/>.
        /// </summary>
        internal RNReactNativeSettingsModule()
        {

        }

        /// <summary>
        /// The name of the native module.
        /// </summary>
        public override string Name
        {
            get
            {
                return "RNReactNativeSettings";
            }
        }
    }
}
