# Compatibility and endpoints

| Area | Behavior |
| --- | --- |
| Android identity | Stable `com.rokstrn.app`. Debug adds `.debug`. Preview adds `.prerelease`. FileProvider and player task affinity use the installed ID. |
| JVM and extension ABI | App classes and generated resources retain `com.lagradost.cloudstream3`. Shared and library namespaces remain for compiled extensions, reflection, and serialization. |
| Local extensions | The developer plugin directory is `<shared-storage>/<application-id>/plugins`. Legacy deployment tools may need that path explicitly. The built-in repository list is empty. |
| Legacy links | Existing schemes and OAuth callbacks are retained. Android can show a chooser if multiple compatible apps are present. TV channel and card intents target the installed package. |
| Backups | Android data is isolated by app ID. Persisted subtitle font keys map to open font equivalents. The credential preferences file is excluded from backup and device transfer. |
| Service credentials | TMDB, OpenSubtitles, MyDramaList and SIMKL can use device-local values. Some tracking providers need separately registered public client IDs. |
| Network behavior | The app, player and downloads enforce certificate verification for HTTPS. Existing plain HTTP media support remains. |

## Endpoint audit

| Endpoint | Scope |
| --- | --- |
| Source, releases, docs, support and community | Fork URL configuration and an optional HTTPS community URL. An unconfigured build shows bundled-source and unavailable-update text. |
| Upstream APK update feed and preview installation | Removed from the fork. Stable updates require the explicitly configured Rokstrn release feed and the same package. |
| Upstream project and API documentation | Clearly labeled attribution and original API provenance. |
| Upstream Wiki, Discord and QuickNovel | Replaced in settings by independent fork links. The upstream source remains available for attribution. |
| Legacy schemes, `cs.repo` and optional jsDelivr proxy | User-directed repository link compatibility. |
| Upstream connectivity check and sharing redirect | Existing connectivity diagnostic and user-directed external sharing. The redirect receives the shared link. |
| Metadata, subtitle, DNS and authentication services | Existing service integrations. Some require credentials or suitable devices. |
| Inherited release and archive workflows | Guarded to the official upstream repository. The fork's workflow only uploads build artifacts. |

Validate install, launch, settings, legal playback, subtitle controls, downloads, backups and lawful extension loading before a public release. Test Cast and Android TV navigation on suitable devices. Pair distributed binaries with their corresponding source and verify the signature and checksums.
