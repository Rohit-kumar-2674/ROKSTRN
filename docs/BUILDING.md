# Build ROKSTRN

## Android-only setup

The Android phone can handle GitHub and Git while GitHub Actions runs the build. If you already have an authorized ROKSTRN fork, use its actual URL and skip creating a new fork. The following commands create a public fork of the public upstream project.

Extract `ROKSTRN-branch.bundle` from the handoff source ZIP into Downloads. Install [Termux](https://github.com/termux/termux-app), open it and run the following. Complete GitHub's web authorization in your phone browser.

```sh
pkg update
pkg install git gh unzip
termux-setup-storage
gh auth login --hostname github.com --git-protocol https --web
gh auth setup-git
gh repo fork recloudstream/cloudstream --fork-name ROKSTRN --clone=false
git clone --branch v4.9.1 https://github.com/recloudstream/cloudstream.git rokstrn
cd rokstrn
git bundle verify "$HOME/storage/downloads/ROKSTRN-branch.bundle"
git fetch "$HOME/storage/downloads/ROKSTRN-branch.bundle" \
  refs/heads/rokstrn-rebrand:refs/heads/rokstrn-rebrand
git switch rokstrn-rebrand
owner=$(gh api user --jq .login)
git remote rename origin upstream
git remote add origin "https://github.com/$owner/ROKSTRN.git"
git push -u origin rokstrn-rebrand
gh repo edit "$owner/ROKSTRN" --default-branch rokstrn-rebrand \
  --description 'Independent GPL-3.0 Android media center based on CloudStream, with original branding and extension support.'
```

On GitHub, enable Actions in the fork if prompted. Open **ROKSTRN Android builds**, select the `rokstrn-rebrand` branch and choose **Run workflow**. Pushes and pull requests targeting the branch also run validation. Download the completed build artifact, extract the ZIP, and open the debug APK on your phone.

## Local SDK build

Install Git, JDK 17 and the [Android command-line tools](https://developer.android.com/tools). Point `JAVA_HOME` and `ANDROID_HOME` to their installed directories.

```sh
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
sdkmanager --licenses
sdkmanager 'platforms;android-37.0' 'build-tools;36.0.0' 'platform-tools'
git clone --branch rokstrn-rebrand "https://github.com/$owner/ROKSTRN.git" rokstrn
cd rokstrn
./gradlew :app:tasks --all
./gradlew :app:assembleStableDebug :app:assembleStableRelease
TZ=UTC ./gradlew :app:lintStableDebug :app:testStableDebugUnitTest :library:jvmTest :library:checkKotlinAbi
scripts/package-apks.sh dist
(cd dist && sha256sum -c SHA256SUMS.txt)
adb install -r dist/ROKSTRN-v4.9.1-rokstrn.1-debug.apk
```

Raw APKs are in `app/build/outputs/apk/stable/debug/` and `app/build/outputs/apk/stable/release/`. The staging script checks application IDs, labels, alignment and available signatures. The upstream preview flavors are also present. They use separate package suffixes and manual installation. If building from a plain source archive, set `ROKSTRN_SOURCE_REVISION` and `SOURCE_DATE_EPOCH` to the originating Git revision and Unix commit time. A local SDK and ADB workflow needs a supported host or cloud runner.

The validation command uses UTC because two inherited episode-date tests fail when the system time zone has a nonzero UTC offset. The library ABI reference reflects the signatures compiled from the official v4.9.1 tag.

## Links, optional updates and signing

Set `rokstrn.github.repository=OWNER/REPOSITORY` in ignored `local.properties` or export `ROKSTRN_GITHUB_REPOSITORY`. An optional `rokstrn.community.url` supplies an HTTPS community link. GitHub Actions sets the fork repository from its run context.

After a verified, signed stable release is available, opt in to stable updates in future builds with `rokstrn.updates.enabled=true`. The feed uses a tag such as `v4.9.1-rokstrn.1` and an exact `ROKSTRN-v4.9.1-rokstrn.1-release.apk` asset with a SHA-256 digest. The updater requires the installed Android package and Android enforces signature compatibility.

Keep your release key outside the repository and retain a private backup. Use your own protected PKCS#12 keystore. The command prompts for a password.

```sh
umask 077
mkdir -p "$HOME/.local/share/rokstrn-signing"
keytool -genkeypair -storetype PKCS12 -keystore "$HOME/.local/share/rokstrn-signing/rokstrn.p12" \
  -alias rokstrn -keyalg RSA -keysize 3072 -validity 10000
```

Set the following four entries in ignored `local.properties` with your private values.

```properties
rokstrn.signing.storeFile=/absolute/private/path/rokstrn.p12
rokstrn.signing.storePassword=<keystore password>
rokstrn.signing.keyAlias=rokstrn
rokstrn.signing.keyPassword=<key password>
```

```sh
./gradlew --no-configuration-cache --no-build-cache :app:assembleStableRelease
scripts/package-apks.sh dist-signed release
"$ANDROID_HOME/build-tools/36.0.0/apksigner" verify --verbose dist-signed/ROKSTRN-v4.9.1-rokstrn.1-release.apk
git archive --format=zip --prefix=ROKSTRN/ -o dist-signed/ROKSTRN-source.zip HEAD
(cd dist-signed && sha256sum ROKSTRN-source.zip >> SHA256SUMS.txt && sha256sum -c SHA256SUMS.txt)
```

For phone-based signing, configure encrypted GitHub repository secrets `ROKSTRN_KEYSTORE_BASE64`, `ROKSTRN_KEYSTORE_PASSWORD`, `ROKSTRN_KEY_ALIAS` and `ROKSTRN_KEY_PASSWORD`. The keystore value is base64 on one line. Run the workflow on the trusted development branch with **Sign the stable release** selected. The action uploads verified artifacts and creates no release automatically.

## Draft release after approval

After verifying the APK signature and the matching corresponding source, and after explicit approval, draft a GitHub Release in the authorized fork.

```sh
gh release create v4.9.1-rokstrn.1 \
  dist-signed/ROKSTRN-v4.9.1-rokstrn.1-release.apk \
  dist-signed/ROKSTRN-source.zip dist-signed/SHA256SUMS.txt \
  --target "$(git rev-parse HEAD)" --draft \
  --title 'ROKSTRN 4.9.1-rokstrn.1' --notes-file RELEASE_NOTES.md
```
