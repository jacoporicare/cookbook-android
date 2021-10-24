package cz.jakubricar.zradelnik.ui.settings

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
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
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.LoggedInUser
import cz.jakubricar.zradelnik.model.Settings
import cz.jakubricar.zradelnik.model.SyncFrequency
import cz.jakubricar.zradelnik.model.Theme
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.login.LoginActivity
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme
import cz.jakubricar.zradelnik.ui.user.UserViewModel
import cz.jakubricar.zradelnik.ui.user.UserViewState

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val viewState by viewModel.state.collectAsState()
    val userViewState by userViewModel.state.collectAsState()
    val launcher = rememberLauncherForActivityResult(LoginActivityResultContract) {
        userViewModel.getLoggedInUser()
    }

    SettingsScreen(
        viewState = viewState,
        userViewState = userViewState,
        onBack = onBack,
        onThemeChange = { viewModel.setTheme(it) },
        onSyncChange = { viewModel.setSync(it) },
        onSyncFrequencyChange = { viewModel.setSyncFrequency(it) },
        onSyncWifiOnlyChange = { viewModel.setSyncWifiOnly(it) },
        onLogin = { launcher.launch(Unit) },
        onLogout = { userViewModel.logout() }
    )
}

@Composable
fun SettingsScreen(
    viewState: SettingsViewState,
    userViewState: UserViewState,
    onBack: () -> Unit,
    onThemeChange: (Theme) -> Unit,
    onSyncChange: (Boolean) -> Unit,
    onSyncFrequencyChange: (SyncFrequency) -> Unit,
    onSyncWifiOnlyChange: (Boolean) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.settings))
                },
                modifier = Modifier.navigationBarsPadding(bottom = false),
                contentPadding = rememberInsetsPaddingValues(
                    LocalWindowInsets.current.statusBars,
                    applyBottom = false
                ),
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
                loggedInUser = userViewState.loggedInUser,
                loadingLoggedInUser = userViewState.loading,
                modifier = Modifier.padding(innerPadding),
                scrollState = scrollState,
                onThemeChange = onThemeChange,
                onSyncChange = onSyncChange,
                onSyncFrequencyChange = onSyncFrequencyChange,
                onSyncWifiOnlyChange = onSyncWifiOnlyChange,
                onLogin = onLogin,
                onLogout = onLogout
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Settings(
    settings: Settings,
    loggedInUser: LoggedInUser?,
    loadingLoggedInUser: Boolean,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onThemeChange: (Theme) -> Unit,
    onSyncChange: (Boolean) -> Unit,
    onSyncFrequencyChange: (SyncFrequency) -> Unit,
    onSyncWifiOnlyChange: (Boolean) -> Unit,
    onLogin: () -> Unit,
    onLogout: () -> Unit,
) {
    var themeDialogOpened by remember { mutableStateOf(false) }
    var syncFrequencyDialogOpened by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .navigationBarsWithImePadding()
    ) {
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
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.settings_account),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colors.secondaryVariant,
            style = MaterialTheme.typography.body2
        )

        when {
            loadingLoggedInUser -> {
                ListItem(
                    text = {
                        CircularProgressIndicator()
                    }
                )
            }
            loggedInUser != null -> {
                ListItem(
                    text = {
                        Text(text = loggedInUser.displayName)
                    },
                    secondaryText = {
                        Text(text = stringResource(R.string.settings_logout))
                    },
                    modifier = Modifier.clickable { onLogout() }
                )
            }
            else -> {
                ListItem(
                    text = {
                        Text(text = stringResource(R.string.settings_login))
                    },
                    modifier = Modifier.clickable { onLogin() }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
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
    val value: T,
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
            loggedInUser = null,
            loadingLoggedInUser = false,
            onThemeChange = { },
            onSyncChange = { },
            onSyncFrequencyChange = { },
            onSyncWifiOnlyChange = { },
            onLogin = { },
            onLogout = { }
        )
    }
}

object LoginActivityResultContract : ActivityResultContract<Unit, Unit>() {

    override fun createIntent(context: Context, input: Unit?): Intent {
        return Intent(context, LoginActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?) {}
}
