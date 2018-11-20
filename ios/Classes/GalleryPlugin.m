#import "GalleryPlugin.h"
#import <gallery/gallery-Swift.h>

@implementation GalleryPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftGalleryPlugin registerWithRegistrar:registrar];
}
@end
