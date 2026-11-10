# Changes

## 4.9.1-rokstrn.1 · 10 November 2026

- Based on official CloudStream `v4.9.1` at `c92e0cf02eda0429fbb4b2fcea4b528cb65aa9fb`.
- Added the separate ROKSTRN app ID, version metadata, launcher artwork, TV banner, splash, notification mark and dark palette.
- Preserved the JVM namespaces, legacy deep links and extension interfaces. Fresh installs have an empty repository list, and local plugin folders are isolated by application ID.
- Added an About section, in-app licenses, source and support configuration, and an opt-in fork-only stable update feed.
- Replaced bundled fonts with absent or restrictive redistribution terms and retained the serialized subtitle font keys.
- Restored standard TLS verification for the app, player and downloads. Replaced embedded service credentials with app-private, device-local settings.
- Added independent signing, APK verification, reproducible revision metadata and phone-usable GitHub Actions.
