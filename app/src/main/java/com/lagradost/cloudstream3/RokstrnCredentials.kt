package com.lagradost.cloudstream3

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.content.edit
import com.lagradost.cloudstream3.metaproviders.configureMetadataCredentials

internal enum class ServiceCredential(@StringRes val label: Int) {
    TmdbReadToken(R.string.rokstrn_credential_tmdb),
    OpenSubtitlesKey(R.string.rokstrn_credential_opensubtitles),
    MyDramaListKey(R.string.rokstrn_credential_mydramalist),
    SimklClientId(R.string.rokstrn_credential_simkl_id),
    SimklClientSecret(R.string.rokstrn_credential_simkl_secret),
}

internal object RokstrnCredentials {
    private const val FILE_NAME = "rokstrn_service_credentials"

    fun get(credential: ServiceCredential): String? = CloudStreamApp.context
        ?.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        ?.getString(credential.name, null)

    fun save(context: Context, credential: ServiceCredential, value: String) {
        val trimmed = value.trim()
        context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).edit {
            if (trimmed.isEmpty()) remove(credential.name) else putString(credential.name, trimmed)
        }
        configureMetadata()
    }

    @OptIn(InternalAPI::class)
    fun configureMetadata() {
        configureMetadataCredentials(
            tmdbToken = get(ServiceCredential.TmdbReadToken),
            myDramaListKey = get(ServiceCredential.MyDramaListKey),
        )
    }
}
