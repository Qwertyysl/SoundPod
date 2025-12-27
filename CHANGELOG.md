# Changelog - Niusic

## [2.1.1] - 2025-12-27

### Added
- **Music Style Presets**: Integrated Equalizer API with Vocal, Music, and Dolby presets accessible via the player.
- **Homepage History**: New "Suggested for you" section featuring recently played songs and improved smart suggestions.
- **Azan Quiet Mode**: Simple 5-minute quiet mode for prayer times without audio files.
- **Donation Support**: Added PayPal integration in the About section.

### Changed
- **Mini Player Refinement**: Reduced button sizes and icon sizes for a cleaner look. Fixed visibility in Light Mode with Glass theme.
- **UI Consistency**: Standardized player top bar layout to fit more controls comfortably.
- **Azan Default**: Updated default azan sound to `azantv3.mp3`.

### Fixed
- **Connectivity**: Resolved network errors on Android 16 and custom ROMs via enhanced security config and retry logic.
- **Lockscreen Lyrics**: Fixed potential crashes and added Apple-style lockscreen artwork support.

## [2.1.0] - 2025-12-27

### Added
- **Android 16 Support**: Initial compatibility for Android 16 (API 36).

### Changed
- **Rebranding**: Complete migration from SoundxFlow to Niusic.
- **Package Update**: Package renamed to `com.github.niusic`.

## [2.0.0] - 2025-12-27

### Added
- **Glass Design Style**: A brand new system-wide iOS-style glass aesthetic with liquid background effects.
- **Adaptive UI**: High-performance glass effects that automatically adapt to light and dark themes.
- **Themed Experience**: New dedicated color schemes for **Spotify** (Green/Black) and **YouTube Music** (Red/Black).
- **Intelligent Contrast**: Mini players now feature smart text contrast based on album art luminance.
- **iOS Liquid Slider**: Completely redesigned Seek Bar with a "liquid lens" thumb and frosted glass track.
- **Frosted Backgrounds**: Player background now extracts and spreads dominant colors from album art for a richer glass effect.

### Changed
- **App Rebranding**: Officially renamed the application to **Niusic**.
- **Homepage Redesign**: Optimized the homepage with high-performance radial gradients for a smooth glass look.
- **Lyrics Visibility**: Improved readability for floating lyrics with a darkened overlay.
- **About Section**: Updated support contacts to `mnxzmi98@gmail.com` and repository links to `Qwertyysl/Niusic`.

### Fixed
- **App Icon**: Resolved an issue where the app icon wasn't updating correctly.
- **Text Visibility**: Fixed unreadable text on the homepage when using certain theme combinations in glass mode.
- **Build & Performance**: Optimized background renders to eliminate frame drops during navigation.
