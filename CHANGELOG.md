# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [2.1.1] - 2025-05-08
Adds support for failovers in Messages API

### Changed
- Bumped Java SDK version to 9.3.1

### Fixed
- Type inference behaviour of `Custom` client

### Deprecated
- SIM Swap API
- Number Verification API
- Verify legacy (v1) API

## [2.1.0] - 2025-04-30

### Added
- Support for custom HTTP requests via `Custom` client

### Changed
- Bumped Java SDK version to 9.2.0

## [2.0.0] - 2025-04-08
Major release for compatibility with [Java SDK v9.0.0](https://github.com/Vonage/vonage-java-sdk/releases/tag/v9.0.0)

### Changed
- Bumped Java SDK version to 9.0.0 (see Java SDK changelog for details of breaking changes)
  - Moved most enums from inner classes
  - Stronger typing for response fields
  - Removed deprecated methods, constructors, classes, fields, packages etc.
  - Internal refactoring
- Refactored `Archive.setLayout` and `Broadcast.setLayout` parameters to take `StreamCompositionLayout` directly

### Removed
- `Verify.whatsappCodeless` workflow
- Pricing API
- `dateStart` and `dateEnd` methods in `CallsFilter` (as previously deprecated)
- `realTimeData` in `NumberInsight.advanced` (as previously deprecated)

## [1.2.1] - 2025-03-19

### Changed
- Bumped Java SDK version to 8.20.0 (support for `quantizationParameter` in Video Archive)

## [1.2.0] - 2025-03-05

### Added
- Support for MMS text, file and multimedia content types in `Messages`

### Changed
- Bumped Java SDK version to 8.18.0

## [1.1.4] - 2025-02-28

### Added
- `Voice.connectToSip` overload with `domain` and `user` parameters

### Changed
- Bumped Java SDK version to 8.17.0

### Deprecated
- `Voice.dateStart` and `Voice.dateEnd` methods (redundant since Java SDK v8.17)

## [1.1.3] - 2025-01-31

### Added
- `NumberInsight.advanced` overload without `realTimeData` parameter

### Changed
- Bumped Java SDK version to 8.16.0

### Deprecated
- `realTimeData` in `NumberInsight.advanced`

## [1.1.2] - 2024-12-12

### Fixed
- `HttpConfig.appendUserAgent` now works as expected in the SDK rather than being overridden

### Removed
- `Application.Builder.removeCapabilities`, since it was added in Java SDK 8.14.0

## [1.1.1] - 2024-12-03
- Bumped Java SDK version to 8.15.0

### Added
- Support for HTTP proxies
- `maxBitrate` property in Video broadcasts

### Changed
- Bumped Kotlin version to 2.1.0

## [1.1.0] - 2024-11-05

### Added
- Pricing API
- Custom template management for Verify API
- Asynchronous DTMF event listener endpoints in Voice API

## [1.0.0] - 2024-10-25
First stable GA release

### Added
- `Voice.downloadRecording(String, Path)` method
- `Application.Builder#networkApis` DSL method

### Changed
- `Sms.wasSuccessfullySent()` now an extension function rather than being part of the client
- `Numbers.listOwned()` now returns `List<OwnedNumber>` instead of `ListNumbersResponse`
- `Numbers.searchAvailable()` now returns `List<AvailableNumber>` instead of `SearchNumbersResponse`

## [1.0.0-RC2] - 2024-09-26
Messages API updates based on Java SDK v8.11.0

### Added
- RCS message type builders
- WhatsApp Reaction builder
- Ability to read and revoke messages (support for the `PATCH` endpoint in Messages API)

### Changed
- User agent string now includes `vonage-kotlin-sdk/$VONAGE_KOTLIN_SDK_VERSION`

## [1.0.0-RC1] - 2024-09-12
First release candidate

### Changed
- Split `layout` method in `Archive.Builder` and `Broadcast.Builder` into 3 separate presets
- Publish Javadoc JAR in Dokka HTML format

## [1.0.0-beta2] - 2024-09-06

### Added
- Documentation (KDocs) for all classes and methods

### Changed
- Moved Video API's `connectToWebSocket`, `startRender` and `sipDial` methods to `ExistingSession`
- `CallsFilter.Builder.dateStart` and `dateEnd` extension functions now accept `Instant` instead of `String`
- `Voice.inputAction` requires body
- `Voice.connectToWebSocket` and `Call.Builder.toWebSocket` `contentType` parameter changed to (mandatory) enum

### Removed
- `Voice.ExistingCall.transfer(URI)` method

## [1.0.0-beta1] - 2024-09-02
Feature-complete beta release

### Added
- Video API

### Changed
- Renamed `VerifyLegacy.ExistingRequest#search` to `info` for consistency with other APIs

### Changed
- Standardised `Existing*` classes to extend `ExistingResource` for consistency.

## [0.9.0] - 2024-08-19

### Added
- Application & Users API

## [0.8.0] - 2024-08-09

### Added
- Subaccounts API

## [0.7.0] - 2024-08-06

### Added
- Numbers API
- Account API

### Changed
- Explicit return types for all methods
- Introduced `ExistingRequest` class to Verify (v2) to reduce duplicating `requestId` parameter

## [0.6.0] - 2024-07-30

### Added
- SIM Swap API
- Number Verification API

### Changed
- `InputAction.Builder#dtmf` extension method uses `DtmfSettings` builder instead of setters
- `Messages#send` now uses optional Boolean parameter for sandbox instead of separate method

## [0.5.0] - 2024-07-25

### Added
- Number Insight v1 API

## [0.4.0] - 2024-07-23

### Added
- Verify v1 API

## [0.3.1] - 2024-07-12

### Changed
- Upgraded Java SDK version to 8.9.2

## [0.3.0] - 2024-07-08

### Added
- SMS API
- Conversion API
- Redact API

### Removed
- `parseInboundMessage`

## [0.2.0] - 2024-07-02

### Added
- Sandbox support in Messages
- Voice API

### Fixed
- `authFromEnv` now checks for absent environment variables before attempting to set them

## [0.1.0] - 2024-06-25
Initial version

### Added
- Messages API
- Verify v2 API
