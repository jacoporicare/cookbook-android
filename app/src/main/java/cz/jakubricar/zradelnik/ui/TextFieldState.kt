package cz.jakubricar.zradelnik.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

open class TextFieldState(
    defaultValue: String? = null,
    private val validator: (String) -> Boolean = { true },
    private val errorFor: (String) -> Int? = { null }
) {

    var value: String by mutableStateOf(defaultValue ?: "")

    // was the TextField ever focused
    var isFocusedDirty: Boolean by mutableStateOf(false)
    var isFocused: Boolean by mutableStateOf(false)
    private var displayErrors: Boolean by mutableStateOf(false)

    open val isValid: Boolean
        get() = validator(value)

    fun onFocusChange(focused: Boolean) {
        isFocused = focused
        if (focused) isFocusedDirty = true
    }

    fun enableShowErrors() {
        // only show errors if the text was at least once focused
        if (isFocusedDirty) {
            displayErrors = true
        }
    }

    fun showErrors() = !isValid && displayErrors

    @StringRes
    open fun getError(): Int? {
        return if (showErrors()) {
            errorFor(value)
        } else {
            null
        }
    }
}
