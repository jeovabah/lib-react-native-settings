#import <React/RCTEventEmitter.h>
#import <React/RCTBridgeModule.h>

@interface RNReactNativeSettings : RCTEventEmitter <RCTBridgeModule>

@property (nonatomic) BOOL hasListeners;

- (BOOL)checkInternetConnection;

@end