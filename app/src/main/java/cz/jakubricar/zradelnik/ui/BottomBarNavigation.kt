package cz.jakubricar.zradelnik.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.size
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.BottomNavigation
import cz.jakubricar.zradelnik.R

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

    BottomNavigation(
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.navigationBars,
        ),
        backgroundColor = MaterialTheme.colors.surface,
    ) {
        screens.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

            val tint = if (selected) {
                MaterialTheme.colors.primary
            } else {
                LocalContentColor.current
            }

            val contentAlpha = if (selected) {
                ContentAlpha.high
            } else {
                ContentAlpha.medium
            }

            BottomNavigationItem(
                icon = {
                    screen.imageVector?.let { imageVector ->
                        Icon(
                            imageVector = imageVector,
                            contentDescription = stringResource(screen.labelId),
                            modifier = Modifier.alpha(contentAlpha),
                            tint = tint
                        )
                    }

                    screen.iconId?.let { iconId ->
                        Icon(
                            painter = painterResource(iconId),
                            contentDescription = stringResource(screen.labelId),
                            modifier = Modifier
                                .size(24.dp)
                                .alpha(contentAlpha),
                            tint = tint,
                        )
                    }
                },
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
                },
                label = {
                    CompositionLocalProvider(LocalContentAlpha provides contentAlpha) {
                        Text(
                            text = stringResource(screen.labelId),
                            modifier = Modifier.alpha(contentAlpha),
                            color = tint,
                        )
                    }
                }
            )
        }
    }
}
