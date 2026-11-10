# ROKSTRN 4.9.1-rokstrn.1

First independent Android build based on CloudStream v4.9.1. Rokstrn includes an original R monogram, TV artwork, separate app identity and in-app open source notices. The inherited player, downloads, media organization and extension architecture remain in the source.

- Stable application ID `com.rokstrn.app`. Debug application ID `com.rokstrn.app.debug`.
- Fresh installs have an empty extension repository list.
- In-app updates are available only in stable builds configured for an independent, approved fork feed.
- Restricted upstream font assets were replaced with redistributable equivalents. Existing subtitle settings retain their serialization keys.
- Optional service credentials are stored on the device. HTTPS connections use standard certificate verification.

The debug APK uses the Android development signature. A stable build without maintainer signing is named `release-unsigned.apk`. Publish only after release signing, verification and explicit approval.
