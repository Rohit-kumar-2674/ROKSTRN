package com.lagradost.cloudstream3.ui.settings

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.lagradost.cloudstream3.CloudStreamApp
import com.lagradost.cloudstream3.CommonActivity.activity
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.RokstrnConfig
import com.mihon.presentation.settings.Preference
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

@Composable
internal fun rokstrnAbout(): Preference {
    var showLicenses by remember { mutableStateOf(false) }
    if (showLicenses) RokstrnLicenses { showLicenses = false }

    val sourceTitle = stringResource(R.string.rokstrn_source)
    val github = R.drawable.ic_github_logo
    val document = R.drawable.description_24px
    return Preference.PreferenceGroup(
        title = stringResource(R.string.rokstrn_about),
        preferenceItems = listOfNotNull(
            Preference.PreferenceItem.TextPreference(
                title = stringResource(R.string.rokstrn_attribution),
                icon = painterResource(R.drawable.ic_launcher_foreground),
            ),
            aboutLink(sourceTitle, RokstrnConfig.sourceUrl, github)
                ?: Preference.PreferenceItem.TextPreference(
                    title = sourceTitle,
                    subtitle = stringResource(R.string.rokstrn_source_unconfigured),
                    enabled = false,
                    icon = painterResource(github),
                ),
            aboutLink(stringResource(R.string.rokstrn_upstream_source), stringResource(R.string.rokstrn_upstream_url), github),
            Preference.PreferenceItem.TextPreference(
                title = stringResource(R.string.rokstrn_open_source_licenses),
                icon = painterResource(document),
                onClick = { showLicenses = true },
            ),
            aboutLink(stringResource(R.string.rokstrn_releases), RokstrnConfig.releasesUrl, R.drawable.mobile_arrow_down_24px),
            aboutLink(stringResource(R.string.rokstrn_documentation), RokstrnConfig.documentationUrl, document),
            aboutLink(stringResource(R.string.rokstrn_support), RokstrnConfig.supportUrl, github),
            aboutLink(stringResource(R.string.rokstrn_community), RokstrnConfig.communityUrl, R.drawable.ic_baseline_discord_24),
        ).toPersistentList(),
    )
}

@Composable
private fun aboutLink(title: String, url: String?, icon: Int): Preference.PreferenceItem.TextPreference? =
    url?.let {
        Preference.PreferenceItem.TextPreference(
            title = title, subtitle = it, icon = painterResource(icon),
            onClick = { CloudStreamApp.openBrowser(it, activity) },
        )
    }

@Composable
private fun RokstrnLicenses(close: () -> Unit) {
    val context = LocalContext.current
    val unavailable = stringResource(R.string.rokstrn_licenses_unavailable)
    var text by remember { mutableStateOf("") }
    LaunchedEffect(context) {
        text = withContext(Dispatchers.IO) {
            try {
                val assets = context.assets
                assets.list("legal").orEmpty().sorted().joinToString("\n\n") { path ->
                    assets.open("legal/$path").bufferedReader().use { it.readText().trimEnd() }
                }.ifBlank { unavailable }
            } catch (_: IOException) {
                unavailable
            }
        }
    }
    AlertDialog(
        onDismissRequest = close,
        title = { Text(stringResource(R.string.rokstrn_open_source_licenses)) },
        text = { Text(text, modifier = Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) },
        confirmButton = { TextButton(onClick = close) { Text(stringResource(R.string.ok)) } },
    )
}
