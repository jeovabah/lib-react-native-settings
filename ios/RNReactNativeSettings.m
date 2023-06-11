#import "RNReactNativeSettings.h"
#import <AVFoundation/AVFoundation.h>
#import <SystemConfiguration/SystemConfiguration.h>

@implementation RNReactNativeSettings

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE();

- (NSArray<NSString *> *)supportedEvents {
  return @[@"VolumeChange", @"NetworkChange"];
}

- (void)startObserving {
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(volumeChanged:)
                                               name:@"AVSystemController_SystemVolumeDidChangeNotification"
                                             object:nil];
  [[NSNotificationCenter defaultCenter] addObserver:self
                                           selector:@selector(reachabilityChanged:)
                                               name:kReachabilityChangedNotification
                                             object:nil];
  self.hasListeners = YES;
}

- (void)stopObserving {
  [[NSNotificationCenter defaultCenter] removeObserver:self];
  self.hasListeners = NO;
}

- (void)volumeChanged:(NSNotification *)notification {
  float currentVolume = [[AVAudioSession sharedInstance] outputVolume];
  float maxVolume = 1.0;
  if (self.hasListeners) {
    [self sendEventWithName:@"VolumeChange"
                       body:@{@"currentVolume": @(currentVolume), @"maxVolume": @(maxVolume)}];
  }
}

- (void)reachabilityChanged:(NSNotification *)note {
  BOOL isConnected = [self checkInternetConnection];
  if (self.hasListeners) {
    [self sendEventWithName:@"NetworkChange"
                       body:@{@"isConnected": @(isConnected)}];
  }
}

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
