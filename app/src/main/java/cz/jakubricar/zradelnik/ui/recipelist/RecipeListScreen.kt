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
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
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
import com.google.accompanist.insets.systemBarsPadding
import com.skydoves.landscapist.ShimmerParams
import com.skydoves.landscapist.coil.CoilImage
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.components.InsetAwareTopAppBar
import cz.jakubricar.zradelnik.ui.components.LoadingContent
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme
import cz.jakubricar.zradelnik.utils.isScrolled

@Composable
fun RecipeListScreen(
    viewModel: RecipeListViewModel = hiltViewModel(),
    navigateToRecipe: (String) -> Unit,
    scaffoldState: ScaffoldState = rememberScaffoldState()
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
        onRefreshRecipes = { viewModel.refreshRecipes() },
        onErrorDismiss = { viewModel.errorShown(it) },
        scaffoldState = scaffoldState
    )
}

@Composable
fun RecipeListScreen(
    uiState: RecipeListUiState,
    recipes: List<List<Recipe>>,
    navigateToRecipe: (String) -> Unit,
    onRefreshRecipes: () -> Unit,
    onErrorDismiss: (Long) -> Unit,
    scaffoldState: ScaffoldState
) {
    val scrollState = rememberLazyListState()

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(hostState = it, modifier = Modifier.systemBarsPadding()) },
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
                backgroundColor = if (!scrollState.isScrolled) {
                    MaterialTheme.colors.background
                } else {
                    MaterialTheme.colors.surface
                },
                elevation = if (!scrollState.isScrolled) 0.dp else 4.dp
            )
        }
    ) { innerPadding ->
        LoadingContent(
            empty = uiState.initialLoad,
            emptyContent = { FullScreenLoading() },
            loading = uiState.loading,
            onRefresh = onRefreshRecipes
        ) {
            RecipeListScreenErrorAndContent(
                recipes = recipes,
                isShowingErrors = uiState.errorMessages.isNotEmpty(),
                onRefresh = onRefreshRecipes,
                navigateToRecipe = navigateToRecipe,
                modifier = Modifier.padding(innerPadding),
                scrollState = scrollState
            )
        }
    }

    // Process one error message at a time and show them as Snackbars in the UI
    if (uiState.errorMessages.isNotEmpty()) {
        // Remember the errorMessage to display on the screen
        val errorMessage = remember(uiState) { uiState.errorMessages[0] }

        // Get the text to show on the message from resources
        val errorMessageText = stringResource(errorMessage.messageId)
        val retryMessageText = stringResource(R.string.try_again)

        // If onRefreshRecipesState or onErrorDismiss change while the LaunchedEffect is running,
        // don't restart the effect and use the latest lambda values.
        val onRefreshRecipesState by rememberUpdatedState(onRefreshRecipes)
        val onErrorDismissState by rememberUpdatedState(onErrorDismiss)

        LaunchedEffect(errorMessage.id, scaffoldState) {
            val snackbarResult = scaffoldState.snackbarHostState.showSnackbar(
                message = errorMessageText,
                actionLabel = retryMessageText
            )
            if (snackbarResult == SnackbarResult.ActionPerformed) {
                onRefreshRecipesState()
            }
            // Once the message is displayed and dismissed, notify the ViewModel
            onErrorDismissState(errorMessage.id)
        }
    }
}

@Composable
private fun RecipeListScreenErrorAndContent(
    recipes: List<List<Recipe>>,
    isShowingErrors: Boolean,
    onRefresh: () -> Unit,
    navigateToRecipe: (String) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: LazyListState
) {
    if (recipes.isNotEmpty()) {
        RecipeList(
            recipes = recipes,
            navigateToRecipe = navigateToRecipe,
            modifier = modifier,
            scrollState = scrollState
        )
    } else if (!isShowingErrors) {
        // if there are no posts, and no error, let the user refresh manually
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.no_recipes),
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.h5
            )
            Button(onClick = onRefresh) {
                Text(text = stringResource(R.string.try_again))
            }
        }
    } else {
        // there's currently an error showing, don't show any content
        Spacer(modifier.fillMaxSize())
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
        modifier = modifier,
        state = scrollState,
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars,
            applyTop = false,
            additionalStart = 16.dp,
            additionalTop = 16.dp,
            additionalEnd = 16.dp,
            additionalBottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            items = recipes,
            key = { it[0].id }
        ) { row ->
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
            .clickable { navigateToRecipe(recipe.slug) }
            .fillMaxHeight(),
        elevation = 4.dp
    ) {
        Column {
            CoilImage(
                imageModel = recipe.imageUrl ?: R.drawable.ic_food_placeholder,
                modifier = Modifier.height(150.dp),
                alignment = Alignment.TopCenter,
                contentDescription = stringResource(R.string.recipe_image, recipe.title),
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
