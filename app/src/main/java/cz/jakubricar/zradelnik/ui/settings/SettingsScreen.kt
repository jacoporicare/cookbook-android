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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.Surface
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
import androidx.navigation.NavController
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.LoggedInUser
import cz.jakubricar.zradelnik.model.Settings
import cz.jakubricar.zradelnik.model.Theme
import cz.jakubricar.zradelnik.ui.BottomBarNavigation
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.login.LoginActivity
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme
import cz.jakubricar.zradelnik.ui.user.UserViewModel
import cz.jakubricar.zradelnik.ui.user.UserViewState

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
) {
    val viewState by viewModel.state.collectAsState()
    val userViewState by userViewModel.state.collectAsState()
    val launcher = rememberLauncherForActivityResult(LoginActivityResultContract) {
        userViewModel.getLoggedInUser()
    }

    SettingsScreen(
        navController = navController,
        viewState = viewState,
        userViewState = userViewState,
        onThemeChange = { viewModel.setTheme(it) },
        onLogin = { launcher.launch(Unit) },
        onLogout = { userViewModel.logout() }
    )
}

@Composable
fun SettingsScreen(
    navController: NavController,
    viewState: SettingsViewState,
    userViewState: UserViewState,
    onThemeChange: (Theme) -> Unit,
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
                backgroundColor = if (scrollState.value == 0) {
                    MaterialTheme.colors.background
                } else {
                    MaterialTheme.colors.surface
                },
                elevation = if (scrollState.value == 0) 0.dp else 4.dp
            )
        },
        bottomBar = {
            BottomBarNavigation(navController = navController)
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
    onLogin: () -> Unit,
    onLogout: () -> Unit,
) {
    var themeDialogOpened by remember { mutableStateOf(false) }

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
            ),
            loggedInUser = null,
            loadingLoggedInUser = false,
            onThemeChange = { },
            onLogin = { },
            onLogout = { }
        )
    }
}

object LoginActivityResultContract : ActivityResultContract<Unit, Unit>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(context, LoginActivity::class.java)
    }

    override fun parseResult(resultCode: Int, intent: Intent?) {}
}
