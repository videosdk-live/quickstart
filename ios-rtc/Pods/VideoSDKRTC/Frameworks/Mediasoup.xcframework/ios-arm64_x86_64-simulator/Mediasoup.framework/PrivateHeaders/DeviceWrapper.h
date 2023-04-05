#ifndef DeviceWrapper_h
#define DeviceWrapper_h

#import <Foundation/Foundation.h>
#import "MediasoupClientMediaKind.h"


@class ReceiveTransportWrapper;
@class SendTransportWrapper;


@interface DeviceWrapper : NSObject

- (BOOL)isLoaded;

- (void)loadWithRouterRTPCapabilities:(NSString *_Nonnull)routerRTPCapabilities
	error:(out NSError *__autoreleasing _Nullable *_Nullable)error
	__attribute__((swift_error(nonnull_error)))
	NS_SWIFT_NAME (load(with:));

- (NSString *_Nullable)sctpCapabilitiesWithError
	:(out NSError *__autoreleasing _Nullable *_Nullable)error;

- (NSString *_Nullable)rtpCapabilitiesWithError
	:(out NSError *__autoreleasing _Nullable *_Nullable)error;

- (BOOL)canProduce:(MediasoupClientMediaKind _Nonnull)mediaKind
	error:(out NSError *__autoreleasing _Nullable *_Nullable)error
	__attribute__((swift_error(nonnull_error)));

- (SendTransportWrapper *_Nullable)createSendTransportWithId:(NSString *_Nonnull)transportId
	iceParameters:(NSString *_Nonnull)iceParameters
	iceCandidates:(NSString *_Nonnull)iceCandidates
	dtlsParameters:(NSString *_Nonnull)dtlsParameters
	sctpParameters:(NSString *_Nullable)sctpParameters
	appData:(NSString *_Nullable)appData
	error:(out NSError *__autoreleasing _Nullable *_Nullable)error;

- (ReceiveTransportWrapper *_Nullable)createReceiveTransportWithId:(NSString *_Nonnull)transportId
	iceParameters:(NSString *_Nonnull)iceParameters
	iceCandidates:(NSString *_Nonnull)iceCandidates
	dtlsParameters:(NSString *_Nonnull)dtlsParameters
	sctpParameters:(NSString *_Nullable)sctpParameters
	appData:(NSString *_Nullable)appData
	error:(out NSError *__autoreleasing _Nullable *_Nullable)error;

@end

#endif /* DeviceWrapper_h */
