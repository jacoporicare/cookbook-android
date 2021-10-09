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
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.navigationBarsHeight
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.Settings
import cz.jakubricar.zradelnik.model.SyncFrequency
import cz.jakubricar.zradelnik.model.Theme
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.components.InsetAwareTopAppBar
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val viewState by viewModel.state.collectAsState()

    SettingsScreen(
        viewState = viewState,
        onBack = onBack,
        onThemeChange = { viewModel.setTheme(it) },
        onSyncChange = { viewModel.setSync(it) },
        onSyncFrequencyChange = { viewModel.setSyncFrequency(it) },
        onSyncWifiOnlyChange = { viewModel.setSyncWifiOnly(it) }
    )
}

@Composable
fun SettingsScreen(
    viewState: SettingsViewState,
    onBack: () -> Unit,
    onThemeChange: (Theme) -> Unit,
    onSyncChange: (Boolean) -> Unit,
    onSyncFrequencyChange: (SyncFrequency) -> Unit,
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
        if (viewState.loading || viewState.settings == null) {
            FullScreenLoading()
        } else {
            Settings(
                settings = viewState.settings,
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
    settings: Settings,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onThemeChange: (Theme) -> Unit,
    onSyncChange: (Boolean) -> Unit,
    onSyncFrequencyChange: (SyncFrequency) -> Unit,
    onSyncWifiOnlyChange: (Boolean) -> Unit
) {
    var themeDialogOpened by remember { mutableStateOf(false) }
    var syncFrequencyDialogOpened by remember { mutableStateOf(false) }

    Column(modifier = modifier.verticalScroll(scrollState)) {
        ListItem(
            text = {
                Text(text = stringResource(R.string.settings_theme))
            },
            secondaryText = {
                Text(
                    text = stringResource(
                        when (settings.theme) {
                            Theme.LIGHT -> R.string.settings_theme_light
                            Theme.DARK -> R.string.settings_theme_dark
                            Theme.DEFAULT -> R.string.settings_theme_default
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
            color = MaterialTheme.colors.secondaryVariant,
            style = MaterialTheme.typography.body2
        )
        ListItem(
            text = {
                Text(text = stringResource(R.string.settings_sync))
            },
            secondaryText = {
                Text(text = stringResource(R.string.settings_sync_last_date, settings.lastSyncDate))
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
                            SyncFrequency.DAILY -> R.string.settings_sync_frequency_daily
                            SyncFrequency.WEEKLY -> R.string.settings_sync_frequency_weekly
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
                    value = Theme.LIGHT
                ),
                ListOption(
                    label = stringResource(R.string.settings_theme_dark),
                    value = Theme.DARK
                ),
                ListOption(
                    label = stringResource(R.string.settings_theme_default),
                    value = Theme.DEFAULT
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
                    value = SyncFrequency.DAILY
                ),
                ListOption(
                    label = stringResource(R.string.settings_sync_frequency_weekly),
                    value = SyncFrequency.WEEKLY
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
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column {
                Text(
                    text = title,
                    modifier = Modifier.padding(
                        start = 24.dp,
                        top = 16.dp,
                        end = 24.dp,
                        bottom = 12.dp
                    ),
                    style = MaterialTheme.typography.h6
                )
                Column(
                    modifier = Modifier
                        .selectableGroup()
                        .padding(bottom = 8.dp)
                ) {
                    options.forEach { option ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = option.value == selectedValue,
                                    onClick = { onSelect(option.value) },
                                    role = Role.RadioButton
                                )
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = option.value == selectedValue,
                                onClick = null,
                                modifier = Modifier.padding(vertical = 12.dp),
                            )
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.body1
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colors.secondary
                        )
                    ) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    ZradelnikTheme {
        Settings(
            settings = Settings(
                theme = Theme.DEFAULT,
                sync = true,
                syncFrequency = SyncFrequency.DAILY,
                syncWifiOnly = true,
                lastSyncDate = "5. 10. 2021 12:56"
            ),
            onThemeChange = { },
            onSyncChange = { },
            onSyncFrequencyChange = { },
            onSyncWifiOnlyChange = { }
        )
    }
}
