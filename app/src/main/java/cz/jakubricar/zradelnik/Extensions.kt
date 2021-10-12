package cz.jakubricar.zradelnik

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

fun Context.getAppSharedPreferences() = AppSharedPreferences(this)

fun Context.getSettingsSharedPreferences() = SettingsSharedPreferences(this)

fun Context.findActivity(): Activity? {
    var context = this

    while (context is ContextWrapper) {
        if (context is Activity) {
            return context
        }

        context = context.baseContext
    }

    return null
}
