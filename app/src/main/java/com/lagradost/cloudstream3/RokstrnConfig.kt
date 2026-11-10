package com.lagradost.cloudstream3

internal object RokstrnConfig {
    const val RELEASE_ASSET_PREFIX = "ROKSTRN-"

    val repository = BuildConfig.ROKSTRN_REPOSITORY.takeIf { it.isNotEmpty() }
    val sourceUrl = repository?.let { "https://github.com/$it" }
    val releasesUrl = sourceUrl?.let { "$it/releases" }
    val documentationUrl = sourceUrl?.let { "$it/blob/rokstrn-rebrand/README.md" }
    val supportUrl = sourceUrl?.let { "$it/issues" }
    val communityUrl = BuildConfig.ROKSTRN_COMMUNITY_URL.takeIf { it.isNotEmpty() }
    val updateFeedUrl = repository
        ?.takeIf { BuildConfig.ROKSTRN_UPDATES_ENABLED && !BuildConfig.DEBUG && BuildConfig.FLAVOR == "stable" }
        ?.let { "https://api.github.com/repos/$it/releases/latest" }
}
