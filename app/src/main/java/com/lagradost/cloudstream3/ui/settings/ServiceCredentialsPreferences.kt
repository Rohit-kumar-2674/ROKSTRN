package com.lagradost.cloudstream3.ui.settings

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.RokstrnCredentials
import com.lagradost.cloudstream3.ServiceCredential
import com.mihon.presentation.settings.Preference
import kotlinx.collections.immutable.toPersistentList

@Composable
internal fun serviceCredentialsPreferences(): Preference {
    val context = LocalContext.current
    var editing by remember { mutableStateOf<ServiceCredential?>(null) }
    var configured by remember {
        mutableStateOf(ServiceCredential.entries.filter { RokstrnCredentials.get(it) != null }.toSet())
    }

    editing?.let { credential ->
        var input by remember(credential) { mutableStateOf("") }
        AlertDialog(
            title = { Text(stringResource(credential.label)) },
            onDismissRequest = { editing = null },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    supportingText = { Text(stringResource(R.string.rokstrn_credential_entry_hint)) },
                )
            },
            confirmButton = {
                TextButton(
                    enabled = input.none(Char::isISOControl),
                    onClick = {
                        RokstrnCredentials.save(context, credential, input)
                        configured = if (input.isBlank()) configured - credential else configured + credential
                        editing = null
                    },
                ) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text(stringResource(R.string.cancel)) } },
        )
    }

    return Preference.PreferenceGroup(
        title = stringResource(R.string.rokstrn_service_credentials),
        preferenceItems = ServiceCredential.entries.map { credential ->
            Preference.PreferenceItem.TextPreference(
                title = stringResource(credential.label),
                subtitle = stringResource(
                    if (credential in configured) R.string.rokstrn_credential_set else R.string.rokstrn_credential_missing
                ),
                onClick = { editing = credential },
            )
        }.toPersistentList(),
    )
}
