package cz.jakubricar.zradelnik.repository

import android.app.Application
import cz.jakubricar.zradelnik.getSettingsSharedPreferences
import cz.jakubricar.zradelnik.model.Settings
import cz.jakubricar.zradelnik.model.Theme
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    app: Application,
) {

    private val prefManager = app.getSettingsSharedPreferences()

    fun getSettings() =
        Settings(
            theme = prefManager.theme,
        )

    fun setTheme(theme: Theme) {
        prefManager.theme = theme
    }
}
