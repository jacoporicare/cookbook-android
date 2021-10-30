package cz.jakubricar.zradelnik.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.res.stringResource
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.utils.ErrorMessage

@Composable
fun ErrorSnackbar(
    errorMessages: List<ErrorMessage>,
    scaffoldState: ScaffoldState,
    onErrorDismiss: (Long) -> Unit,
) {
    if (errorMessages.isEmpty()) {
        return
    }

    // Remember the errorMessage to display on the screen
    val errorMessage = remember(errorMessages) { errorMessages[0] }

    // Get the text to show on the message from resources
    val errorMessageText = stringResource(errorMessage.messageId)
    val retryMessageText = stringResource(R.string.try_again)

    // If onTryAgain or onErrorDismiss change while the LaunchedEffect is running,
    // don't restart the effect and use the latest lambda values.
    val onTryAgainState by rememberUpdatedState(errorMessage.onTryAgain)
    val onErrorDismissState by rememberUpdatedState(onErrorDismiss)

    LaunchedEffect(errorMessage.id, scaffoldState) {
        val result = scaffoldState.snackbarHostState.showSnackbar(
            message = errorMessageText,
            actionLabel = errorMessage.onTryAgain?.let { retryMessageText }
        )

        if (result == SnackbarResult.ActionPerformed) {
            onTryAgainState?.invoke()
        }

        // Once the message is displayed and dismissed, notify the ViewModel
        onErrorDismissState(errorMessage.id)
    }
}
