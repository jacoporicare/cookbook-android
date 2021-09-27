package cz.jakubricar.zradelnik.ui.recipelist

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.coil.CoilImage
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.ui.LoadingState
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.components.InsetAwareTopAppBar
import cz.jakubricar.zradelnik.ui.components.LoadingContent
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme
import cz.jakubricar.zradelnik.utils.isScrolled

@Composable
fun RecipeListScreen(
    navigateToRecipe: (String) -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val recipes = remember(uiState.recipes, searchQuery) {
        filterRecipes(uiState.recipes, searchQuery).chunked(2)
    }

    RecipeListScreen(
        uiState = uiState,
        recipes = recipes,
        navigateToRecipe = navigateToRecipe,
        onRefreshRecipes = { viewModel.refreshRecipes() }
    )
}

@Composable
fun RecipeListScreen(
    uiState: RecipeListUiState,
    recipes: List<List<Recipe>>,
    navigateToRecipe: (String) -> Unit,
    onRefreshRecipes: () -> Unit
) {
    val scrollState = rememberLazyListState()

    Scaffold(
        topBar = {
            InsetAwareTopAppBar(
                title = {
                    Text(text = stringResource(R.string.app_name))
                },
                actions = {
                    IconButton(onClick = { /* TODO: Open search */ }) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.action_search)
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
                elevation = if (!scrollState.isScrolled) 0.dp else 4.dp
            )
        }
    ) { innerPadding ->
        LoadingContent(
            empty = uiState.initialLoad,
            emptyContent = { FullScreenLoading() },
            loading = uiState.loadingState == LoadingState.LOADING,
            onRefresh = onRefreshRecipes
        ) {
            // TODO: Display errors, like [HomeScreenErrorAndContent]
            RecipeList(
                recipes = recipes,
                navigateToRecipe = navigateToRecipe,
                modifier = Modifier.padding(innerPadding),
                scrollState = scrollState
            )
        }
    }
}

@Composable
fun RecipeList(
    recipes: List<List<Recipe>>,
    navigateToRecipe: (String) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: LazyListState
) {
    LazyColumn(
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars,
            applyTop = false,
            additionalTop = 16.dp,
            additionalBottom = 16.dp,
            additionalStart = 12.dp,
            additionalEnd = 12.dp
        ),
        modifier = modifier,
        state = scrollState,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = recipes,
            key = { it[0].id }
        ) { row ->
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Recipe(
                    modifier = Modifier.weight(1f),
                    recipe = row[0],
                    navigateToRecipe = navigateToRecipe
                )

                if (row.size > 1) {
                    Recipe(
                        modifier = Modifier.weight(1f),
                        recipe = row[1],
                        navigateToRecipe = navigateToRecipe
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun Recipe(
    modifier: Modifier = Modifier,
    recipe: Recipe,
    navigateToRecipe: (String) -> Unit,
) {
    Card(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clickable { navigateToRecipe(recipe.slug) }
            .fillMaxHeight(),
        elevation = 4.dp
    ) {
        Column {
            CoilImage(
                imageModel = recipe.imageUrl ?: R.drawable.ic_food_placeholder,
                alignment = Alignment.TopCenter,
                modifier = Modifier.height(150.dp),
                shimmerParams = ShimmerParams(
                    baseColor = MaterialTheme.colors.background,
                    highlightColor = MaterialTheme.colors.onBackground,
                    durationMillis = 700,
                    dropOff = 0.65f,
                    tilt = 20f
                ),
                failure = {
                    Image(
                        painter = painterResource(R.drawable.ic_broken_image),
                        contentDescription = stringResource(R.string.image_failure),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            )
            Text(
                text = recipe.title,
                style = MaterialTheme.typography.subtitle1,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(heightDp = 200)
@Composable
fun DefaultPreview() {
    ZradelnikTheme {
        Recipe(
            recipe = Recipe(
                id = "1",
                slug = "slug",
                title = "Koprovka",
                imageUrl = "https://develop.api.zradelnik.eu/image/koprova-omacka_60b625cd71cc4b28a638d432?size=640x640&format=webp"
            ),
            navigateToRecipe = {}
        )
    }
}
