package cz.jakubricar.zradelnik.utils

import androidx.annotation.StringRes

data class ErrorMessage(
    val id: Long,
    @StringRes val messageId: Int,
    val tryAgain: Boolean = false,
)
