package cz.jakubricar.zradelnik.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateListOf
import cz.jakubricar.zradelnik.utils.ErrorMessage
import java.util.UUID

class ErrorState {

    val errorMessages = mutableStateListOf<ErrorMessage>()

    fun addError(@StringRes messageId: Int, onTryAgain: (() -> Unit)? = null) {
        errorMessages.add(
            ErrorMessage(
                id = UUID.randomUUID().mostSignificantBits,
                messageId = messageId,
                onTryAgain = onTryAgain,
            )
        )
    }

    fun errorShown(errorId: Long) {
        errorMessages.removeIf { it.id == errorId }
    }
}
