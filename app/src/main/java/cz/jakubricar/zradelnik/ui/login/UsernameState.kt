package cz.jakubricar.zradelnik.ui.login

import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.ui.TextFieldState

class UsernameState(defaultValue: String? = null) :
    TextFieldState(
        defaultValue = defaultValue,
        validator = ::isUsernameValid,
        errorFor = ::usernameValidationError
    )

private fun isUsernameValid(username: String): Boolean =
    username.isNotEmpty()

@Suppress("UNUSED_PARAMETER")
private fun usernameValidationError(username: String): Int =
    R.string.login_invalid_username
