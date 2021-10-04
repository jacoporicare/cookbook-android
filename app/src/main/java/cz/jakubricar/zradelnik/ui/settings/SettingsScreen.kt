package cz.jakubricar.zradelnik.ui.settings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
        onBack = onBack
    )
}

@Composable
fun SettingsScreen(
    settings: Settings?,
    loading: Boolean,
    onBack: () -> Unit
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
                scrollState = scrollState
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
    scrollState: ScrollState = rememberScrollState()
) {
    val dateFormat = DateFormat.getDateTimeInstance(
        DateFormat.DEFAULT,
        DateFormat.SHORT,
        Locale.getDefault()
    )
    // TODO: use settings
    val lastSync = dateFormat.format(Instant.now().epochSecond * 1000)

    Column(modifier = modifier.verticalScroll(scrollState)) {
        ListItem(
            text = { Text(text = stringResource(R.string.settings_theme)) },
            secondaryText = { Text(text = stringResource(R.string.settings_theme_default)) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.settings_data_usage),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colors.secondary,
            style = MaterialTheme.typography.body2
        )
        ListItem(
            text = { Text(text = stringResource(R.string.settings_sync)) },
            secondaryText = {
                Text(text = stringResource(R.string.settings_sync_last_date, lastSync))
            },
            trailing = { Switch(checked = true, onCheckedChange = {}) }
        )
        Divider()
        ListItem(
            text = { Text(text = stringResource(R.string.settings_sync_frequency)) },
            secondaryText = {
                Text(text = stringResource(R.string.settings_sync_frequency_daily))
            }
        )
        Divider()
        ListItem(
            text = { Text(text = stringResource(R.string.settings_sync_wifi_only)) },
            secondaryText = {
                Text(text = stringResource(R.string.settings_sync_wifi_only_summary))
            },
            trailing = { Switch(checked = true, onCheckedChange = {}) }
        )
        Spacer(modifier = Modifier.navigationBarsHeight())
    }
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
            )
        )
    }
}
