package cz.jakubricar.zradelnik.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cz.jakubricar.zradelnik.ui.recipe.RecipeScreen
import cz.jakubricar.zradelnik.ui.recipe.RecipeViewModel
import cz.jakubricar.zradelnik.ui.recipe.RecipeViewModel.Companion.RECIPE_SLUG_KEY
import cz.jakubricar.zradelnik.ui.recipelist.RecipeListScreen

object MainDestinations {
    const val RECIPE_LIST_ROUTE = "recipelist"
    const val RECIPE_ROUTE = "recipe"
}

@Composable
fun ZradelnikNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = MainDestinations.RECIPE_LIST_ROUTE
) {
    val actions = remember(navController) { MainActions(navController) }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(MainDestinations.RECIPE_LIST_ROUTE) {
            RecipeListScreen(
                navigateToRecipe = actions.navigateToRecipe
            )
        }
        composable(
            route = "${MainDestinations.RECIPE_ROUTE}/{$RECIPE_SLUG_KEY}",
            arguments = listOf(navArgument(RECIPE_SLUG_KEY) { type = NavType.StringType })
        ) { backStackEntry ->
            val slug = backStackEntry.arguments?.getString(RECIPE_SLUG_KEY)!!
            val viewModel: RecipeViewModel = hiltViewModel()

            RecipeScreen(
                slug = slug,
                viewModel = viewModel,
                onBack = actions.upPress
            )
        }
    }
}

class MainActions(navController: NavHostController) {
    val navigateToRecipe: (String) -> Unit = { slug: String ->
        navController.navigate("${MainDestinations.RECIPE_ROUTE}/$slug")
    }

    val upPress: () -> Unit = {
        navController.navigateUp()
    }
}
