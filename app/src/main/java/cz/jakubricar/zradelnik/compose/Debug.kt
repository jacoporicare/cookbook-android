@file:Suppress("NOTHING_TO_INLINE")

package cz.jakubricar.zradelnik.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import cz.jakubricar.zradelnik.BuildConfig
import timber.log.Timber

class Ref(var value: Int)

const val EnableDebugCompositionLogs = false

/**
 * An effect which logs the number compositions at the invoked point of the slot table.
 * Thanks to [objcode](https://github.com/objcode) for this code.
 *
 * This is an inline function to act as like a C-style #include to the host composable function.
 * That way we track it's compositions, not this function's compositions.
 *
 * @param tag Prefix used for [Timber.d]
 */
@Composable
inline fun LogCompositions(tag: String) {
    if (EnableDebugCompositionLogs && BuildConfig.DEBUG) {
        val ref = remember { Ref(0) }
        SideEffect { ref.value++ }
        Timber.d("$tag: Compositions: ${ref.value}")
    }
}
