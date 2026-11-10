# ROKSTRN

<img src="branding/rokstrn.svg" width="96" height="96" alt="Rokstrn R monogram">

Rokstrn is an independent Android media center based on CloudStream v4.9.1, with original artwork, subtitle controls, media organization, downloads and an extensible playback architecture.

## Android support

Rokstrn targets Android phones, tablets and Android TV. The minimum OS is Android 6.0 (API 23). Upstream playback, search, watchlists, downloads, external-player integration and Cast remain part of the project. Fresh installs ship without streaming sources or a preconfigured extension repository.

## Build and install

The build uses JDK 17, Gradle 9.4.1, AGP 9.1.1, Kotlin 2.4.0, Android SDK 37.0 and Build Tools 36.0.0.

```sh
./gradlew :app:assembleStableDebug :app:assembleStableRelease
./gradlew :app:lintStableDebug :app:testStableDebugUnitTest :library:jvmTest :library:checkKotlinAbi
scripts/package-apks.sh dist
```

Download the matching artifact from your fork's **Actions** page or its **Releases** page when available. Open the verified debug or maintainer-signed APK on Android and follow the installation prompt. The development APK has its own `com.rokstrn.app.debug` app ID. The stable app uses `com.rokstrn.app`. An unsigned release requires signing before Android can install it.

See [Build and release](docs/BUILDING.md) for the phone-only workflow, source import, signing and verification. See [Compatibility](docs/COMPATIBILITY.md) for device-dependent checks and the endpoint audit.

## Privacy, extensions and contributing

Rokstrn adds no analytics, advertising SDK or Android permissions. The app contacts configured extensions and the providers needed by the features you use. HTTPS connections verify certificates. Supported HTTP media still work. Installed extensions execute code in the app process. Service credentials live in separate device-local preferences excluded from backups and device transfer.

Open bug reports in the Rokstrn fork you use. Include the APK version and variant, Git revision, Android device or emulator and reproducible steps. Remove private URLs and credentials from logs. Test contributions and follow the retained [AI disclosure policy](AI-POLICY.md).

## Source and license

Rokstrn is a modified fork of [CloudStream](https://github.com/recloudstream/cloudstream), licensed under [GPL-3.0](LICENSE). The official base is [v4.9.1](https://github.com/recloudstream/cloudstream/tree/v4.9.1) at `c92e0cf02eda0429fbb4b2fcea4b528cb65aa9fb`. Copyright remains with the original contributors and applicable third parties.

See [CHANGES.md](CHANGES.md), [NOTICE](NOTICE), [third-party notices](THIRD_PARTY_NOTICES.md), and [release notes](RELEASE_NOTES.md). In-app credit and license text are under **Settings → General → About Rokstrn**.
