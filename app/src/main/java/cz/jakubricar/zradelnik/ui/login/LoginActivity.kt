package cz.jakubricar.zradelnik.ui.login

import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cz.jakubricar.zradelnik.ui.ZradelnikApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {

    companion object {

        const val PARAM_USERNAME = "username"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val accountAuthenticatorResponse = intent.getParcelableExtra<AccountAuthenticatorResponse?>(
            AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE
        )?.apply {
            onRequestContinued()
        }

        val isNewAccount = intent.getStringExtra(PARAM_USERNAME) == null
        val username = intent.getStringExtra(PARAM_USERNAME)

        setContent {
            ZradelnikApp(enableEdgeToEdge = ::enableEdgeToEdge) {
                LoginScreen(
                    isNewAccount = isNewAccount,
                    defaultUsername = username,
                    onResult = {
                        accountAuthenticatorResponse?.onResult(it.extras)
                        setResult(RESULT_OK, it)
                        finish()
                    },
                    onBack = { onBackPressedDispatcher.onBackPressed() }
                )
            }
        }
    }
}
