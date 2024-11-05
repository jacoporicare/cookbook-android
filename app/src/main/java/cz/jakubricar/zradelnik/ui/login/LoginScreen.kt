package cz.jakubricar.zradelnik.ui.login

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.auth.AccountAuthenticator
import cz.jakubricar.zradelnik.autofill
import cz.jakubricar.zradelnik.ui.TextFieldState
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    isNewAccount: Boolean,
    defaultUsername: String?,
    onResult: (intent: Intent) -> Unit,
    onBack: () -> Unit,
) {
    val viewState by viewModel.state.collectAsState()
    val usernameState = remember { UsernameState(defaultUsername) }
    val passwordState = remember { PasswordState() }

    LoginScreen(
        viewState = viewState,
        usernameState = usernameState,
        passwordState = passwordState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onSubmit = {
            viewModel.login(usernameState.value, passwordState.value)
        }
    )

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarLoginFailedMessage = stringResource(R.string.login_failed)

    LaunchedEffect(viewState.loginResult) {
        viewState.loginResult
            ?.onSuccess { token ->
                val intent = finishLogin(
                    context = context,
                    isNewAccount = isNewAccount,
                    username = usernameState.value,
                    password = passwordState.value,
                    token = token
                )

                onResult(intent)
            }
            ?.onFailure {
                viewModel.resetLoginResult()

                scope.launch {
                    snackbarHostState.showSnackbar(snackbarLoginFailedMessage)
                }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewState: LoginViewState,
    usernameState: UsernameState,
    passwordState: PasswordState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.login))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
//                backgroundColor = if (scrollState.value == 0) {
//                    MaterialTheme.colors.background
//                } else {
//                    MaterialTheme.colors.surface
//                },
//                elevation = if (scrollState.value == 0) 0.dp else 4.dp
            )
        }
    ) { innerPadding ->
        LoginScreen(
            loading = viewState.loading,
            usernameState = usernameState,
            passwordState = passwordState,
            modifier = Modifier.padding(innerPadding),
            scrollState = scrollState,
            onSubmit = onSubmit
        )
    }
}

@Composable
fun LoginScreen(
    loading: Boolean,
    usernameState: UsernameState,
    passwordState: PasswordState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    onSubmit: () -> Unit,
) {
    Column {
        if (loading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
//        } else {
//            Spacer(modifier = Modifier.height(ProgressIndicatorDefaults.StrokeWidth))
        }

        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = modifier
                .verticalScroll(scrollState)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            Username(usernameState = usernameState)
            Spacer(modifier = Modifier.height(16.dp))
            Password(
                passwordState = passwordState,
                onDone = { onSubmit() }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onSubmit() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TextFieldDefaults.MinHeight),
                enabled = usernameState.isValid && passwordState.isValid && !loading
            ) {
                Text(
                    text = stringResource(id = R.string.login_login)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Username(
    usernameState: TextFieldState = remember { UsernameState() },
) {
    TextField(
        value = usernameState.value,
        onValueChange = {
            usernameState.value = it
        },
        label = { Text(text = stringResource(R.string.login_username)) },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                usernameState.onFocusChange(focusState.isFocused)

                if (!focusState.isFocused) {
                    usernameState.enableShowErrors()
                }
            }
            .autofill(listOf(AutofillType.Username)) { usernameState.value = it },
        textStyle = MaterialTheme.typography.bodyMedium,
        isError = usernameState.showErrors(),
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            imeAction = ImeAction.Next
        ),
    )

    usernameState.getError()?.let { error -> TextFieldError(textError = stringResource(error)) }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Password(
    passwordState: TextFieldState,
    onDone: () -> Unit = {},
) {
    val showPassword = remember { mutableStateOf(false) }

    TextField(
        value = passwordState.value,
        onValueChange = {
            passwordState.value = it
            passwordState.enableShowErrors()
        },
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                passwordState.onFocusChange(focusState.isFocused)

                if (!focusState.isFocused) {
                    passwordState.enableShowErrors()
                }
            }
            .autofill(listOf(AutofillType.Password)) { passwordState.value = it },
        textStyle = MaterialTheme.typography.bodyMedium,
        label = { Text(text = stringResource(R.string.login_password)) },
        trailingIcon = {
            if (showPassword.value) {
                IconButton(onClick = { showPassword.value = false }) {
                    Icon(
                        imageVector = Icons.Filled.Visibility,
                        contentDescription = stringResource(R.string.login_password_hide)
                    )
                }
            } else {
                IconButton(onClick = { showPassword.value = true }) {
                    Icon(
                        imageVector = Icons.Filled.VisibilityOff,
                        contentDescription = stringResource(R.string.login_password_show)
                    )
                }
            }
        },
        visualTransformation = if (showPassword.value) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        isError = passwordState.showErrors(),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Password
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() })
    )

    passwordState.getError()?.let { error -> TextFieldError(textError = stringResource(error)) }
}

/**
 * To be removed when [TextField]s support error
 */
@Composable
fun TextFieldError(textError: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = textError,
            modifier = Modifier.fillMaxWidth(),
            style = LocalTextStyle.current.copy(color = MaterialTheme.colorScheme.error)
        )
    }
}

private fun finishLogin(
    context: Context,
    isNewAccount: Boolean,
    username: String,
    password: String,
    token: String,
): Intent {
    val account = Account(username, AccountAuthenticator.ACCOUNT_TYPE)
    val accountManager = AccountManager.get(context)

    if (isNewAccount) {
        accountManager.addAccountExplicitly(account, password, null)
        accountManager.setAuthToken(account, AccountAuthenticator.AUTH_TOKEN_TYPE, token)
    } else {
        accountManager.setPassword(account, password)
    }

    return Intent().apply {
        putExtra(AccountManager.KEY_ACCOUNT_NAME, username)
        putExtra(AccountManager.KEY_ACCOUNT_TYPE, AccountAuthenticator.ACCOUNT_TYPE)
    }
}
