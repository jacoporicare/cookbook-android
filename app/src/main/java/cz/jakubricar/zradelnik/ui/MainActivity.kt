package cz.jakubricar.zradelnik.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cz.jakubricar.zradelnik.ui.recipe.RecipeViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // including IME animations, and go edge-to-edge
        // This also sets up the initial system bar style based on the platform theme
        enableEdgeToEdge()

        val recipeId = intent.extras?.getString("recipe_id")

        setContent {
            ZradelnikApp(enableEdgeToEdge = ::enableEdgeToEdge) {
                ZradelnikNavGraph(
                    startDestination = recipeId?.let { "${MainDestinations.RECIPE_ROUTE}/{${RecipeViewModel.RECIPE_ID_KEY}}" }
                        ?: MainDestinations.RECIPE_LIST_ROUTE,
                    recipeId = recipeId,
                )
            }
        }
    }
}
