<p align="center">
  <img src="branding/rokstrn.svg" width="112" height="112" alt="Rokstrn R monogram">
</p>

<h1 align="center">ROKSTRN</h1>

<p align="center">An independent, GPL-3.0 Android media center built on CloudStream.</p>

<p align="center">
  <a href="#get-rokstrn">Get Rokstrn</a> ·
  <a href="#build-from-source">Build from source</a> ·
  <a href="docs/COMPATIBILITY.md">Compatibility</a> ·
  <a href="LICENSE">License</a>
</p>

Rokstrn brings playback, subtitles, watchlists, downloads, and user-configured extensions together in an Android app with original artwork and a separate app identity. It supports Android phones, tablets, and Android TV from Android 6.0 (API 23). Fresh installs start with an empty extension repository list.

## Get Rokstrn

Open **Releases** in the repository sidebar to find published APKs, matching source, and SHA-256 checksums. Download the APK to your Android device and open it to install. For a manual integrity check, keep `SHA256SUMS.txt` beside the downloaded files and run `sha256sum -c SHA256SUMS.txt`.

| Build | Android package | Intended use |
| --- | --- | --- |
| Debug preview | `com.rokstrn.app.debug` | Installable development build with Android debug signing. |
| Stable | `com.rokstrn.app` | Distribution build signed with the project's maintainer key. |

The two packages install separately and maintain separate app data. An unsigned build requires signing before installation. Review the release notes for the verification status and limitations of each published APK.

## Features and technology

| Area | What Rokstrn uses |
| --- | --- |
| Playback | AndroidX Media3 and NextLib for media playback, audio and subtitle controls, and external-player integration. |
| Interface | Kotlin, Compose, and Android Views for browsing, search, watchlists, downloads, and Android TV navigation. |
| Extensions | CloudStream's extension interfaces and loader, with the binary API preserved for compatible extensions. Users choose the lawful repositories they configure. |
| Rokstrn changes | Original R monogram and TV artwork, independent app IDs and versioning, optional service credentials, verified HTTPS, and a fork-specific update configuration. |
| Build and release | Gradle wrapper, Android SDK tooling, and GitHub Actions for builds, APK checks, and optional maintainer signing. |

Device coverage varies by release. See [compatibility and endpoint notes](docs/COMPATIBILITY.md).

## Build from source

Use JDK 17, the Gradle 9.4.1 wrapper, Android Gradle Plugin 9.1.1, Kotlin 2.4.0, Android SDK 37.0, and Build Tools 36.0.0.

```sh
./gradlew :app:assembleStableDebug :app:assembleStableRelease
TZ=UTC ./gradlew :app:lintStableDebug :app:testStableDebugUnitTest :library:jvmTest :library:checkKotlinAbi
scripts/package-apks.sh dist
```

The [build and signing guide](docs/BUILDING.md) covers building from an Android phone with GitHub Actions, configuring signing, and verifying release artifacts.

## Privacy and contributions

Rokstrn adds no analytics, advertising SDKs, or Android permissions. It contacts configured extensions and services used by the features you enable. App-owned HTTPS clients verify certificates. Service credentials stay in device-local preferences excluded from Android backups and device transfers. Installed extensions execute in the app process. Review the source and publisher before installing them.

Use **Issues** for bug reports. Include the Rokstrn version and variant, Android version, and steps to reproduce. Remove private URLs and credentials from logs. Test code contributions and follow the retained [AI disclosure policy](AI-POLICY.md).

## Credits and license

Rokstrn is an independently maintained modification of [CloudStream](https://github.com/recloudstream/cloudstream), based on [v4.9.1](https://github.com/recloudstream/cloudstream/tree/v4.9.1) at `c92e0cf02eda0429fbb4b2fcea4b528cb65aa9fb`. Original contributors and third parties retain their copyright. The project is licensed under [GPL-3.0](LICENSE).

Read [changes](CHANGES.md), [release notes](RELEASE_NOTES.md), [attribution](NOTICE), and [third-party notices](THIRD_PARTY_NOTICES.md). In the app, open **Settings → General → About Rokstrn** for source and license information.
