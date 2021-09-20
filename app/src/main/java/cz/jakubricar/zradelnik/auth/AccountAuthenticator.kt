package cz.jakubricar.zradelnik.auth

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.repository.UserRepository
import cz.jakubricar.zradelnik.ui.login.LoginActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AccountAuthenticator(
    private val context: Context,
    private val userRepository: UserRepository
) : AbstractAccountAuthenticator(context) {
    companion object {
        const val ACCOUNT_TYPE = "cz.jakubricar.zradelnik"
        const val AUTH_TOKEN_TYPE = "jwt"
    }

    @DelicateCoroutinesApi
    override fun addAccount(
        response: AccountAuthenticatorResponse,
        accountType: String,
        authTokenType: String?,
        requiredFeatures: Array<String>?,
        options: Bundle
    ): Bundle {
        val account = AccountManager.get(context).getAccountsByType(ACCOUNT_TYPE).firstOrNull()
        if (account != null) {
            val errorMessage = context.getString(R.string.login_account_already_exists)

            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }

            return Bundle().apply {
                putInt(
                    AccountManager.KEY_ERROR_CODE,
                    AccountManager.ERROR_CODE_UNSUPPORTED_OPERATION
                )
                putString(AccountManager.KEY_ERROR_MESSAGE, errorMessage)
            }
        }

        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
        }

        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    override fun confirmCredentials(
        arg0: AccountAuthenticatorResponse,
        arg1: Account,
        arg2: Bundle
    ): Bundle {
        return Bundle.EMPTY
    }

    override fun editProperties(arg0: AccountAuthenticatorResponse, arg1: String): Bundle {
        return Bundle.EMPTY
    }

    override fun getAuthToken(
        response: AccountAuthenticatorResponse,
        account: Account,
        authTokenType: String,
        options: Bundle
    ): Bundle {
        val accountManager = AccountManager.get(context)
        var authToken = accountManager.peekAuthToken(account, authTokenType)

        if (authToken.isNullOrEmpty()) {
            val password = accountManager.getPassword(account)
            if (password != null) {
                authToken = runBlocking {
                    userRepository.login(account.name, password).getOrNull()
                }
            }
        }

        if (!authToken.isNullOrEmpty()) {
            return Bundle().apply {
                putString(AccountManager.KEY_ACCOUNT_NAME, account.name)
                putString(AccountManager.KEY_ACCOUNT_TYPE, account.type)
                putString(AccountManager.KEY_AUTHTOKEN, authToken)
            }
        }

        val intent = Intent(context, LoginActivity::class.java).apply {
            putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            putExtra(LoginActivity.PARAM_USERNAME, account.name)
        }

        return Bundle().apply {
            putParcelable(AccountManager.KEY_INTENT, intent)
        }
    }

    override fun getAuthTokenLabel(arg0: String): String {
        return ""
    }

    override fun hasFeatures(
        arg0: AccountAuthenticatorResponse,
        arg1: Account,
        arg2: Array<String>
    ): Bundle {
        return Bundle.EMPTY
    }

    override fun updateCredentials(
        arg0: AccountAuthenticatorResponse,
        arg1: Account,
        arg2: String,
        arg3: Bundle
    ): Bundle {
        return Bundle.EMPTY
    }
}
