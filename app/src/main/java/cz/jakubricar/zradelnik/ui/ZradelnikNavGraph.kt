package cz.jakubricar.zradelnik.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import cz.jakubricar.zradelnik.ui.recipe.RecipeScreen
import cz.jakubricar.zradelnik.ui.recipe.RecipeViewModel.Companion.RECIPE_ID_KEY
import cz.jakubricar.zradelnik.ui.recipeedit.RecipeEditScreen
import cz.jakubricar.zradelnik.ui.recipeedit.RecipeEditViewModel.Companion.RECIPE_EDIT_ID_KEY
import cz.jakubricar.zradelnik.ui.recipelist.RecipeListScreen
import cz.jakubricar.zradelnik.ui.settings.SettingsScreen
import cz.jakubricar.zradelnik.ui.user.UserViewModel

object MainDestinations {

    const val RECIPE_LIST_ROUTE = "recipelist"
    const val RECIPE_ROUTE = "recipe"
    const val RECIPE_ADD_ROUTE = "recipeadd"
    const val RECIPE_EDIT_ROUTE = "recipeedit"
    const val SETTINGS_ROUTE = "settings"
}

const val WEB_URI = "https://www.zradelnik.eu"

@Composable
fun ZradelnikNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = MainDestinations.RECIPE_LIST_ROUTE,
) {
    val actions = remember(navController) { MainActions(navController) }
    val userViewModel: UserViewModel = hiltViewModel()

    // Refresh logged in user - e.g. when the account is removed via system account settings
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                userViewModel.getLoggedInUser()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = MainDestinations.RECIPE_LIST_ROUTE,
            deepLinks = listOf(navDeepLink { uriPattern = WEB_URI })
        ) {
            RecipeListScreen(
                userViewModel = userViewModel,
                onNavigateToRecipe = actions.navigateToRecipe,
                onNavigateToRecipeAdd = actions.navigateToRecipeAdd,
                onNavigateToSettings = actions.navigateToSettings
            )
        }
        composable(
            route = "${MainDestinations.RECIPE_ROUTE}/{$RECIPE_ID_KEY}",
            arguments = listOf(navArgument(RECIPE_ID_KEY) { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "$WEB_URI/recept/{$RECIPE_ID_KEY}" }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(RECIPE_ID_KEY)!!

            RecipeScreen(
                userViewModel = userViewModel,
                id = id,
                onBack = actions.upPress,
                onNavigateToRecipeEdit = actions.navigateToRecipeEdit
            )
        }
        composable(route = MainDestinations.RECIPE_ADD_ROUTE) {
            RecipeEditScreen(
                userViewModel = userViewModel,
                onBack = actions.upPress,
                onNavigateToRecipe = actions.navigateAfterEditToRecipe,
            )
        }
        composable(
            route = "${MainDestinations.RECIPE_EDIT_ROUTE}/{$RECIPE_EDIT_ID_KEY}",
            arguments = listOf(navArgument(RECIPE_EDIT_ID_KEY) { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString(RECIPE_EDIT_ID_KEY)!!

            RecipeEditScreen(
                userViewModel = userViewModel,
                id = id,
                onBack = actions.upPress,
                onNavigateToRecipe = actions.navigateAfterEditToRecipe,
            )
        }
        composable(
            route = MainDestinations.SETTINGS_ROUTE
        ) {
            SettingsScreen(
                userViewModel = userViewModel,
                onBack = actions.upPress
            )
        }
    }
}

class MainActions(navController: NavHostController) {

    val navigateToRecipe: (String) -> Unit = { id: String ->
        navController.navigate("${MainDestinations.RECIPE_ROUTE}/$id")
    }

    val navigateToRecipeAdd: () -> Unit = {
        navController.navigate(MainDestinations.RECIPE_ADD_ROUTE)
    }

    val navigateToRecipeEdit: (String) -> Unit = { id: String ->
        navController.navigate("${MainDestinations.RECIPE_EDIT_ROUTE}/$id")
    }

    val navigateAfterEditToRecipe: (String) -> Unit = { id: String ->
        navController.navigate("${MainDestinations.RECIPE_ROUTE}/$id") {
            popUpTo(MainDestinations.RECIPE_LIST_ROUTE)
        }
    }

    val navigateToSettings: () -> Unit = {
        navController.navigate(MainDestinations.SETTINGS_ROUTE)
    }

    val upPress: () -> Unit = {
        navController.navigateUp()
    }
}
