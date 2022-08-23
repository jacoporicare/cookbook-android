package cz.jakubricar.zradelnik.repository

import android.accounts.AccountManager
import android.accounts.AccountManager.KEY_AUTHTOKEN
import android.content.Context
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import cz.jakubricar.zradelnik.LoginMutation
import cz.jakubricar.zradelnik.MeQuery
import cz.jakubricar.zradelnik.auth.AccountAuthenticator.Companion.ACCOUNT_TYPE
import cz.jakubricar.zradelnik.auth.AccountAuthenticator.Companion.AUTH_TOKEN_TYPE
import cz.jakubricar.zradelnik.model.LoggedInUser
import cz.jakubricar.zradelnik.network.toResult
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class UserRepository @Inject constructor(
    private val apolloClient: ApolloClient,
) {

    suspend fun login(username: String, password: String): Result<String> =
        apolloClient.mutation(LoginMutation(username, password))
            .execute()
            .toResult()
            .map { it.login.token }

    fun logout(context: Context) {
        val accountManager = AccountManager.get(context)
        val account = accountManager.getAccountsByType(ACCOUNT_TYPE).firstOrNull() ?: return

        accountManager.removeAccountExplicitly(account)
    }

    suspend fun getLoggedInUser(token: String): Result<LoggedInUser> =
        try {
            apolloClient.query(MeQuery())
                .fetchPolicy(FetchPolicy.NetworkFirst)
                .addHttpHeader("Authorization", "Bearer $token")
                .execute()
                .toResult()
                .map { data ->
                    LoggedInUser(
                        id = data.me.id,
                        displayName = data.me.displayName
                    )
                }
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun getAuthToken(context: Context): String? {
        val accountManager = AccountManager.get(context)
        val account = accountManager.getAccountsByType(ACCOUNT_TYPE).firstOrNull() ?: return null

        return suspendCoroutine { cont ->
            accountManager.getAuthToken(
                account,
                AUTH_TOKEN_TYPE,
                null,
                false,
                {
                    cont.resume(
                        if (it.isCancelled) null
                        else try {
                            it.result?.getString(KEY_AUTHTOKEN)
                        } catch (e: Exception) {
                            Timber.e(e)
                            null
                        }
                    )
                },
                null
            )
        }
    }
}
