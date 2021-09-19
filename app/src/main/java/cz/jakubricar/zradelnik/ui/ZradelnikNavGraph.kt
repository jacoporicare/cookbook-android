package cz.jakubricar.zradelnik.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cz.jakubricar.zradelnik.ui.home.HomeScreen
import cz.jakubricar.zradelnik.ui.recipe.RecipeScreen
import cz.jakubricar.zradelnik.ui.recipe.RecipeViewModel.Companion.RECIPE_ID_KEY

object MainDestinations {
    const val HOME_ROUTE = "home"
    const val RECIPE_ROUTE = "recipe"
    const val SETTINGS_ROUTE = "settings"
}


@Composable
fun ZradelnikNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = MainDestinations.HOME_ROUTE
) {
    val actions = remember(navController) { MainActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(MainDestinations.HOME_ROUTE) {
            HomeScreen(
                navigateToRecipe = actions.navigateToRecipe
            )
        }
        composable(
            route = "${MainDestinations.RECIPE_ROUTE}/{$RECIPE_ID_KEY}",
            arguments = listOf(navArgument(RECIPE_ID_KEY) { type = NavType.StringType })
        ) { backStackEntry ->
            RecipeScreen(
                recipeId = backStackEntry.arguments?.getString(RECIPE_ID_KEY)!!,
                onBack = actions.upPress
            )
        }
    }
}

class MainActions(navController: NavHostController) {
    val navigateToRecipe: (String) -> Unit = { recipeId: String ->
        navController.navigate("${MainDestinations.RECIPE_ROUTE}/$recipeId")
    }

    val upPress: () -> Unit = {
        navController.navigateUp()
    }
}
