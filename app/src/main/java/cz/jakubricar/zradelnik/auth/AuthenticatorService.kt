package cz.jakubricar.zradelnik.auth

import android.accounts.AccountManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import cz.jakubricar.zradelnik.repository.UserRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AuthenticatorService : Service() {
    @Inject
    lateinit var userRepository: UserRepository

    override fun onBind(intent: Intent): IBinder? =
        when (intent.action) {
            AccountManager.ACTION_AUTHENTICATOR_INTENT ->
                AccountAuthenticator(this, userRepository).iBinder
            else -> null
        }
}
