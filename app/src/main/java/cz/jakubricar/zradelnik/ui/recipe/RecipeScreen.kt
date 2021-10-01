package cz.jakubricar.zradelnik.ui.recipe

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.RecipeDetail
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.components.InsetAwareTopAppBar
import cz.jakubricar.zradelnik.utils.isScrolled

@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel = hiltViewModel(),
    slug: String,
    onBack: () -> Unit
) {
    LaunchedEffect(slug) {
        // The app opened for the first time, navigate to the list to fetch recipes
        if (viewModel.initialSync) {
            onBack()
        } else {
            viewModel.getRecipe(slug)
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    RecipeScreen(
        recipe = uiState.recipe,
        loading = uiState.loading,
        onBack = onBack
    )

    LaunchedEffect(uiState) {
        if (uiState.failedLoading) {
            onBack()
        }
    }
}

@Composable
fun RecipeScreen(
    recipe: RecipeDetail?,
    loading: Boolean,
    onBack: () -> Unit
) {
    val scrollState = rememberLazyListState()

    Scaffold(
        topBar = {
            InsetAwareTopAppBar(
                title = {
                    Text(text = recipe?.title ?: stringResource(R.string.app_name))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                backgroundColor = if (!scrollState.isScrolled) {
                    MaterialTheme.colors.background
                } else {
                    MaterialTheme.colors.surface
                },
                elevation = if (!scrollState.isScrolled) 0.dp else 4.dp
            )
        }
    ) { innerPadding ->
        if (loading || recipe == null) {
            FullScreenLoading()
        } else {
            Recipe(
                recipe = recipe,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun Recipe(
    recipe: RecipeDetail,
    modifier: Modifier = Modifier,
) {
    Text(
        text = recipe.directions ?: "",
        modifier = modifier
    )
}
