package cz.jakubricar.zradelnik.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

//        val recipeId = intent.extras?.get("recipe_id")

        setContent {
            ZradelnikApp {
                ZradelnikNavGraph(
//                    startDestination = recipeId?.let { "${MainDestinations.RECIPE_ROUTE}/${recipeId}" }
//                        ?: MainDestinations.RECIPE_LIST_ROUTE
                )
            }
        }
    }
}
