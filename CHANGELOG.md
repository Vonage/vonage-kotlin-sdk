# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

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

Initial version.

### Added
- Messages API
- Verify v2 API

