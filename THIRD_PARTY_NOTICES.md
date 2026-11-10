# Third-party notices

The upstream [GPL-3.0 license](LICENSE) and copyright headers are retained. **Settings → General → About Rokstrn → Open source licenses** displays the files in [app/src/main/assets/legal](app/src/main/assets/legal).

| Component | Provenance |
| --- | --- |
| CloudStream Android app, API and shared UI | [Official v4.9.1 source](https://github.com/recloudstream/cloudstream/tree/v4.9.1) at `c92e0cf02eda0429fbb4b2fcea4b528cb65aa9fb`. The corresponding fork source accompanies the APK. |
| NextLib | GPL-3.0. [Version 1.9.3-0.12.0 source and build scripts](https://github.com/anilbeesetti/nextlib/tree/175beb9811ce5712ddb0003e0d6cfb6845f8bdcc). |
| FFmpeg | [FFmpeg 6.0 source](https://ffmpeg.org/releases/ffmpeg-6.0.tar.gz) as fetched by the [NextLib native build script](https://github.com/anilbeesetti/nextlib/blob/175beb9811ce5712ddb0003e0d6cfb6845f8bdcc/ffmpeg/setup.sh). |
| NewPipe Extractor | GPL-3.0. [Version v0.26.3](https://github.com/TeamNewPipe/NewPipeExtractor/tree/v0.26.3). |
| TorrentServer | Inherited dependency pinned at [7861970e038b35cd8c6918384e49caf26903e09e](https://github.com/recloudstream/torrentserver/tree/7861970e038b35cd8c6918384e49caf26903e09e). Its README identifies its GPL-3.0 lineage. |
| AndroidX, Media3, Compose and Google Cast | Versions are in the [version catalog](gradle/libs.versions.toml). The inherited Google SDK is subject to the [Cast SDK terms](https://developers.google.com/cast/docs/terms). |
| Vendored settings and color picker | The [Mihon notice](shared/src/commonMain/kotlin/com/mihon/LICENSE) and [color picker notice](shared/src/commonMain/kotlin/com/lagradost/cloudstream4/compose/colorpicker/LICENSE) are retained. |

The inherited Open Sans binary has an Apache-2.0 notice. Poppins uses OFL-1.1, Ubuntu uses the Ubuntu Font Licence 1.0 and STIX General retains its embedded notice. The ten replacement fonts are unmodified OFL-1.1 assets from [Google Fonts at commit 5bb2873](https://github.com/google/fonts/tree/5bb28733cec313b63fbd415b574b5ae35c8aa8b8). The [Tinos project](https://github.com/googlefonts/tinos) provides its accompanying notice. Original ROKSTRN brand artwork and editable sources are under [branding](branding).
