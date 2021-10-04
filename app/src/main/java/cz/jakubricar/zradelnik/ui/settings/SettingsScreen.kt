package cz.jakubricar.zradelnik.ui.settings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.navigationBarsHeight
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.Settings
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.components.InsetAwareTopAppBar
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme
import java.text.DateFormat
import java.time.Instant
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreen(
        settings = uiState.settings,
        loading = uiState.loading,
        onBack = onBack,
        onThemeChange = { viewModel.setTheme(it) },
        onSyncChange = { viewModel.setSync(it) },
        onSyncFrequencyChange = { viewModel.setSyncFrequency(it) },
        onSyncWifiOnlyChange = { viewModel.setSyncWifiOnly(it) }
    )
}

@Composable
fun SettingsScreen(
    settings: Settings?,
    loading: Boolean,
    onBack: () -> Unit,
    onThemeChange: (Settings.Theme) -> Unit,
    onSyncChange: (Boolean) -> Unit,
    onSyncFrequencyChange: (Settings.SyncFrequency) -> Unit,
    onSyncWifiOnlyChange: (Boolean) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            InsetAwareTopAppBar(
                title = {
                    Text(text = stringResource(R.string.settings))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                backgroundColor = if (scrollState.value == 0) {
                    MaterialTheme.colors.background
                } else {
                    MaterialTheme.colors.surface
                },
                elevation = if (scrollState.value == 0) 0.dp else 4.dp
            )
        }
    ) { innerPadding ->
        if (loading || settings == null) {
            FullScreenLoading()
        } else {
            Settings(
                settings = settings,
                modifier = Modifier.padding(innerPadding),
                scrollState = scrollState,
                onThemeChange = onThemeChange,
                onSyncChange = onSyncChange,
                onSyncFrequencyChange = onSyncFrequencyChange,
                onSyncWifiOnlyChange = onSyncWifiOnlyChange
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Settings(
    // TODO: use settings
    settings: Settings,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onThemeChange: (Settings.Theme) -> Unit,
    onSyncChange: (Boolean) -> Unit,
    onSyncFrequencyChange: (Settings.SyncFrequency) -> Unit,
    onSyncWifiOnlyChange: (Boolean) -> Unit
) {
    var themeDialogOpened by remember { mutableStateOf(false) }
    var syncFrequencyDialogOpened by remember { mutableStateOf(false) }

    val dateFormat = DateFormat.getDateTimeInstance(
        DateFormat.DEFAULT,
        DateFormat.SHORT,
        Locale.getDefault()
    )
    // TODO: use settings
    val lastSync = dateFormat.format(Instant.now().epochSecond * 1000)

    Column(modifier = modifier.verticalScroll(scrollState)) {
        ListItem(
            text = {
                Text(text = stringResource(R.string.settings_theme))
            },
            secondaryText = {
                Text(
                    text = stringResource(
                        when (settings.theme) {
                            Settings.Theme.LIGHT -> R.string.settings_theme_light
                            Settings.Theme.DARK -> R.string.settings_theme_dark
                            Settings.Theme.DEFAULT -> R.string.settings_theme_default
                        }
                    )
                )
            },
            modifier = Modifier.clickable { themeDialogOpened = true }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.settings_data_usage),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colors.secondary,
            style = MaterialTheme.typography.body2
        )
        ListItem(
            text = {
                Text(text = stringResource(R.string.settings_sync))
            },
            secondaryText = {
                Text(text = stringResource(R.string.settings_sync_last_date, lastSync))
            },
            trailing = {
                Switch(
                    checked = settings.sync,
                    onCheckedChange = onSyncChange
                )
            },
            modifier = Modifier.clickable { onSyncChange(!settings.sync) }
        )
        Divider()
        ListItem(
            text = {
                Text(text = stringResource(R.string.settings_sync_frequency))
            },
            secondaryText = {
                Text(
                    text = stringResource(
                        when (settings.syncFrequency) {
                            Settings.SyncFrequency.DAILY -> R.string.settings_sync_frequency_daily
                            Settings.SyncFrequency.WEEKLY -> R.string.settings_sync_frequency_weekly
                        }
                    )
                )
            },
            modifier = Modifier.clickable { syncFrequencyDialogOpened = true }
        )
        Divider()
        ListItem(
            text = {
                Text(text = stringResource(R.string.settings_sync_wifi_only))
            },
            secondaryText = {
                Text(text = stringResource(R.string.settings_sync_wifi_only_summary))
            },
            trailing = {
                Switch(
                    checked = settings.syncWifiOnly,
                    onCheckedChange = onSyncWifiOnlyChange
                )
            },
            modifier = Modifier.clickable { onSyncWifiOnlyChange(!settings.syncWifiOnly) }
        )
        Spacer(modifier = Modifier.navigationBarsHeight())
    }

    if (themeDialogOpened) {
        ListSettingsDialog(
            title = stringResource(R.string.settings_theme),
            options = listOf(
                ListOption(
                    label = stringResource(R.string.settings_theme_light),
                    value = Settings.Theme.LIGHT
                ),
                ListOption(
                    label = stringResource(R.string.settings_theme_dark),
                    value = Settings.Theme.DARK
                ),
                ListOption(
                    label = stringResource(R.string.settings_theme_default),
                    value = Settings.Theme.DEFAULT
                )
            ),
            selectedValue = settings.theme,
            onSelect = {
                themeDialogOpened = false
                onThemeChange(it)
            },
            onDismiss = { themeDialogOpened = false }
        )
    }

    if (syncFrequencyDialogOpened) {
        ListSettingsDialog(
            title = stringResource(R.string.settings_sync_frequency),
            options = listOf(
                ListOption(
                    label = stringResource(R.string.settings_sync_frequency_daily),
                    value = Settings.SyncFrequency.DAILY
                ),
                ListOption(
                    label = stringResource(R.string.settings_sync_frequency_weekly),
                    value = Settings.SyncFrequency.WEEKLY
                )
            ),
            selectedValue = settings.syncFrequency,
            onSelect = {
                syncFrequencyDialogOpened = false
                onSyncFrequencyChange(it)
            },
            onDismiss = { syncFrequencyDialogOpened = false }
        )
    }
}

data class ListOption<T>(
    val label: String,
    val value: T
)

@Composable
fun <T> ListSettingsDialog(
    title: String,
    options: List<ListOption<T>>,
    selectedValue: T,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.h6
            )
        },
        text = {
            Column(
                modifier = Modifier.selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                for (option in options) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = option.value == selectedValue,
                                onClick = { onSelect(option.value) },
                                role = Role.RadioButton
                            ),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option.value == selectedValue,
                            onClick = null
                        )
                        Text(
                            text = option.label,
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }
        },
        buttons = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    ZradelnikTheme {
        Settings(
            settings = Settings(
                theme = Settings.Theme.DEFAULT,
                sync = true,
                syncFrequency = Settings.SyncFrequency.DAILY,
                syncWifiOnly = true
            ),
            onThemeChange = { },
            onSyncChange = { },
            onSyncFrequencyChange = { },
            onSyncWifiOnlyChange = { }
        )
    }
}
