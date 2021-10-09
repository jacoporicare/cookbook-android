package cz.jakubricar.zradelnik.ui.recipe

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.navigationBarsHeight
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

    val viewState by viewModel.state.collectAsState()

    RecipeScreen(
        viewState = viewState,
        onBack = onBack
    )

    LaunchedEffect(viewState) {
        if (viewState.failedLoading) {
            onBack()
        }
    }
}

@Composable
fun RecipeScreen(
    viewState: RecipeViewState,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            InsetAwareTopAppBar(
                title = {
                    Text(text = viewState.recipe?.title ?: stringResource(R.string.recipe))
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
        if (viewState.loading || viewState.recipe == null) {
            FullScreenLoading()
        } else {
            Recipe(
                recipe = viewState.recipe,
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
                failure = {
                    Image(
                        painter = painterResource(R.drawable.ic_broken_image),
                        contentDescription = stringResource(R.string.image_failure),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
        }

        if (!recipe.preparationTime.isNullOrEmpty() ||
            !recipe.servingCount.isNullOrEmpty() ||
            !recipe.sideDish.isNullOrEmpty()
        ) {
            Details(
                modifier = Modifier.padding(top = 16.dp),
                preparationTime = recipe.preparationTime,
                servingCount = recipe.servingCount,
                sideDish = recipe.sideDish
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            if (recipe.ingredients.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.ingredients),
                    modifier = Modifier.padding(bottom = 8.dp),
                    style = MaterialTheme.typography.h6
                )
                Card(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    elevation = 2.dp
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Ingredients(ingredients = recipe.ingredients)
                    }
                }
            }

            Text(
                text = stringResource(R.string.directions),
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.h6
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 2.dp
            ) {
                Box(modifier = Modifier.padding(8.dp)) {
                    MarkdownText(
                        markdown = recipe.directions ?: stringResource(R.string.no_directions)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.navigationBarsHeight())
    }
}

@Composable
private fun Details(
    modifier: Modifier = Modifier,
    preparationTime: String? = null,
    servingCount: String? = null,
    sideDish: String? = null,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        preparationTime?.let {
            DetailItem(
                label = stringResource(R.string.preparation_time),
                value = it
            )
        }
        servingCount?.let {
            DetailItem(
                label = stringResource(R.string.serving_count),
                value = it
            )
        }
        sideDish?.let {
            DetailItem(
                label = stringResource(R.string.side_dish),
                value = it
            )
        }
    }
}

@Composable
private fun DetailItem(
    label: String,
    value: String
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = label,
                style = MaterialTheme.typography.body2
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.body2
        )
    }
}


@Composable
private fun Ingredients(
    ingredients: List<RecipeDetail.Ingredient>
) {
    Row {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Column(horizontalAlignment = Alignment.End) {
                ingredients.forEach { ingredient ->
                    Text(
                        text = ingredient.amount ?: "",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                ingredients.forEach { ingredient ->
                    Text(
                        text = ingredient.amountUnit ?: "",
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            ingredients.forEach { ingredient ->
                val alpha = if (ingredient.isGroup) ContentAlpha.medium else ContentAlpha.high

                CompositionLocalProvider(LocalContentAlpha provides alpha) {
                    Text(
                        text = ingredient.name,
                        fontWeight = if (ingredient.isGroup) FontWeight.Bold else FontWeight.Normal,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}
