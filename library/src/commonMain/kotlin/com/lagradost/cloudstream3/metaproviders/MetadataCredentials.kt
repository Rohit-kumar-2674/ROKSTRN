package com.lagradost.cloudstream3.metaproviders

import com.lagradost.cloudstream3.InternalAPI
import kotlin.concurrent.Volatile

internal object MetadataCredentials {
    @Volatile var tmdbToken: String? = null
    @Volatile var myDramaListKey: String? = null
}

@InternalAPI
fun configureMetadataCredentials(tmdbToken: String?, myDramaListKey: String?) {
    MetadataCredentials.tmdbToken = tmdbToken
    MetadataCredentials.myDramaListKey = myDramaListKey
}
