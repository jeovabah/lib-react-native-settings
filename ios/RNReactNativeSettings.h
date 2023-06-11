#import "RNReactNativeSettings.h"
#import <AVFoundation/AVFoundation.h>
#import <SystemConfiguration/SystemConfiguration.h>

@implementation RNReactNativeSettings

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(getVolumeState:(RCTResponseSenderBlock)callback)
{
  float currentVolume = [[AVAudioSession sharedInstance] outputVolume];
  float maxVolume = 1.0;

  NSDictionary *volumeState = @{
    @"currentVolume": @(currentVolume),
    @"maxVolume": @(maxVolume)
  };

  callback(@[volumeState]);
}

RCT_EXPORT_METHOD(hasInternetConnection:(RCTResponseSenderBlock)callback)
{
  BOOL isConnected = [self checkInternetConnection];

  callback(@[@(isConnected)]);
}

- (BOOL)checkInternetConnection
{
  SCNetworkReachabilityRef reachabilityRef = SCNetworkReachabilityCreateWithName(NULL, "www.google.com");
  SCNetworkReachabilityFlags flags;
  BOOL success = SCNetworkReachabilityGetFlags(reachabilityRef, &flags);
  BOOL isReachable = success && (flags & kSCNetworkFlagsReachable) && !(flags & kSCNetworkFlagsConnectionRequired);
  CFRelease(reachabilityRef);

  return isReachable;
}

@end
