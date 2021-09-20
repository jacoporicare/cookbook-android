package cz.jakubricar.zradelnik.lifecycle

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class LiveDataValidator(
    private val liveData: LiveData<String>,
    private val isLiveDataDirty: LiveData<Boolean>
) {
    private val validationRules = mutableListOf<(value: String?) -> Boolean>()
    private val errorMessages = mutableListOf<Int>()

    val error = MutableLiveData<Int?>()

    // For checking if the liveData value matches the error condition set in the validation rule predicate
    // The liveData value is said to be valid when its value doesn't match an error condition set in the predicate
    fun isValid(): Boolean {
        for (i in 0 until validationRules.size) {
            if (validationRules[i](liveData.value)) {
                // For not touched liveData we want to return false but no error message just yet
                // Otherwise all inputs would show an error after first touch of any input
                emitErrorMessage(if (isLiveDataDirty.value == true) errorMessages[i] else null)
                return false
            }
        }

        emitErrorMessage(null)
        return true
    }

    // For emitting error messages
    private fun emitErrorMessage(@StringRes resId: Int?) {
        error.value = resId
    }

    // For adding validation rules
    fun addRule(@StringRes resId: Int, predicate: (value: String?) -> Boolean) {
        validationRules.add(predicate)
        errorMessages.add(resId)
    }
}
