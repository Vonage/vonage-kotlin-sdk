# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.0.0] - 2024-10-?? (expected)
First stable GA release

### Changed
- `Sms.wasSuccessfullySent()` now an extension function rather than being part of the client

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
