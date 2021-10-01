package cz.jakubricar.zradelnik.ui.recipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.navigationBarsHeight
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.coil.CoilImage
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.RecipeDetail
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.components.InsetAwareTopAppBar
import dev.jeziellago.compose.markdowntext.MarkdownText

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
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            InsetAwareTopAppBar(
                title = {
                    Text(text = recipe?.title ?: stringResource(R.string.recipe_screen_title))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                backgroundColor = if (scrollState.value == 0) {
                    MaterialTheme.colors.background
                } else {
                    MaterialTheme.colors.surface
                },
                elevation = if (scrollState.value == 0) 0.dp else 4.dp
            )
        }
    ) { innerPadding ->
        if (loading || recipe == null) {
            FullScreenLoading()
        } else {
            Recipe(
                recipe = recipe,
                modifier = Modifier.padding(innerPadding),
                scrollState = scrollState
            )
        }
    }
}

private val imageHeight = 300.dp

@Composable
fun Recipe(
    recipe: RecipeDetail,
    modifier: Modifier = Modifier,
    scrollState: ScrollState
) {
    Column(modifier = modifier.verticalScroll(scrollState)) {
        recipe.imageUrl?.let { imageUrl ->
            CoilImage(
                imageModel = imageUrl,
                modifier = Modifier.height(imageHeight),
                alignment = Alignment.TopCenter,
                contentDescription = stringResource(R.string.recipe_image, recipe.title),
                shimmerParams = ShimmerParams(
                    baseColor = MaterialTheme.colors.background,
                    highlightColor = MaterialTheme.colors.onBackground,
                    dropOff = 0.65f,
                    tilt = 20f,
                    durationMillis = 700
                ),
                failure = {
                    Image(
                        painter = painterResource(R.drawable.ic_broken_image),
                        contentDescription = stringResource(R.string.image_failure),
                        modifier = Modifier
                            .height(imageHeight)
                            .fillMaxWidth()
                    )
                }
            )
        }
        Column(modifier = Modifier.padding(16.dp)) {
            if (recipe.ingredients.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.ingredients),
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = MaterialTheme.typography.h6
                )
                Ingredients(
                    ingredients = recipe.ingredients,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Text(
                text = stringResource(R.string.directions),
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.h6
            )
            MarkdownText(markdown = recipe.directions ?: stringResource(R.string.no_directions))
        }
        Spacer(modifier = Modifier.navigationBarsHeight())
    }
}

@Composable
private fun Ingredients(
    ingredients: List<RecipeDetail.Ingredient>,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier) {
        Column(horizontalAlignment = Alignment.End) {
            for (ingredient in ingredients) {
                Text(
                    text = ingredient.amount ?: "",
                    style = MaterialTheme.typography.body2
                )
            }
        }
        Column(modifier = Modifier.padding(start = 8.dp)) {
            for (ingredient in ingredients) {
                Text(
                    text = ingredient.amountUnit ?: "",
                    style = MaterialTheme.typography.body2
                )
            }
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            for (ingredient in ingredients) {
                Text(
                    text = ingredient.name,
                    style = MaterialTheme.typography.body2
                )
            }
        }
    }
}
