package cz.jakubricar.zradelnik

import android.content.Context

fun Context.getAppSharedPreferences() = AppSharedPreferences(this)

fun Context.getSettingsSharedPreferences() = SettingsSharedPreferences(this)
