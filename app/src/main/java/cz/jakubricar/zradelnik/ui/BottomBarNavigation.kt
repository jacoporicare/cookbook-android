package cz.jakubricar.zradelnik.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ui.Scaffold
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme

sealed class Screen(
    val route: String,
    @StringRes val labelId: Int,
    val imageVector: ImageVector? = null,
    @DrawableRes val iconId: Int? = null,
) {

    object RecipeList : Screen(
        route = MainDestinations.RECIPE_LIST_ROUTE,
        labelId = R.string.recipes,
        imageVector = Icons.Outlined.MenuBook,
    )

    object InstantPotRecipeList : Screen(
        route = MainDestinations.INSTANT_POT_RECIPE_LIST_ROUTE,
        labelId = R.string.instant_pot,
        iconId = R.drawable.ic_multicooker,
    )

    object Settings : Screen(
        route = MainDestinations.SETTINGS_ROUTE,
        labelId = R.string.settings,
        imageVector = Icons.Outlined.Settings,
    )
}

val screens = listOf(
    Screen.RecipeList,
    Screen.InstantPotRecipeList,
    Screen.Settings,
)

@Composable
fun BottomBarNavigation(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar {
        screens.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            NavigationBarItem(
                icon = {
                    screen.imageVector?.let { imageVector ->
                        Icon(
                            imageVector = imageVector,
                            contentDescription = stringResource(screen.labelId),
                        )
                    }

                    screen.iconId?.let { iconId ->
                        Icon(
                            painter = painterResource(iconId),
                            contentDescription = stringResource(screen.labelId),
                            modifier = Modifier.size(24.dp),
                        )
                    }
                },
                label = { Text(text = stringResource(screen.labelId)) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}


@Preview(heightDp = 200)
@Composable
fun DefaultPreview() {
    ZradelnikTheme {
        Scaffold(
            bottomBar = { BottomBarNavigation(navController = rememberNavController()) }
        ) {
            Text(text = "Bottom Bar Navigation")
        }

    }
}
