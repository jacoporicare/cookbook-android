package cz.jakubricar.zradelnik.ui.recipelist

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.derivedWindowInsetsTypeOf
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.compose.LogCompositions
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.ui.components.ExpandableFloatingActionButton
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.components.LoadingContent
import cz.jakubricar.zradelnik.ui.components.floatingActionButtonSize
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme
import cz.jakubricar.zradelnik.ui.user.UserViewModel
import cz.jakubricar.zradelnik.ui.user.UserViewState

@Composable
fun RecipeListScreen(
    viewModel: RecipeListViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    onNavigateToRecipe: (String) -> Unit = {},
    onNavigateToRecipeAdd: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    LogCompositions("RecipeListScreen")
    val viewState by viewModel.state.collectAsState()
    val userViewState by userViewModel.state.collectAsState()

    BackHandler(viewState.searchVisible) {
        viewModel.hideSearch()
    }

    RecipeListScreen(
        viewState = viewState,
        userViewState = userViewState,
        scaffoldState = scaffoldState,
        onNavigateToRecipe = onNavigateToRecipe,
        onNavigateToRecipeAdd = onNavigateToRecipeAdd,
        onNavigateToSettings = onNavigateToSettings,
        onRefreshRecipes = { viewModel.refreshRecipes() },
        onErrorDismiss = { viewModel.errorShown(it) },
        onSearchShow = { viewModel.showSearch() },
        onSearchHide = { viewModel.hideSearch() },
        onSearchQueryChange = { viewModel.setSearchQuery(it) }
    )
}

@Composable
fun RecipeListScreen(
    viewState: RecipeListViewState,
    userViewState: UserViewState,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    onNavigateToRecipe: (String) -> Unit = {},
    onNavigateToRecipeAdd: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onRefreshRecipes: () -> Unit = {},
    onErrorDismiss: (Long) -> Unit = {},
    onSearchShow: () -> Unit = {},
    onSearchHide: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
) {
    LogCompositions("RecipeListScreenStateless")
    val recipes = rememberFilteredRecipes(viewState.recipes, viewState.searchQuery)
    val listState = rememberLazyListState()

    val fabVisible = userViewState.loggedInUser != null

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(
                hostState = it,
                modifier = if (!fabVisible) {
                    Modifier.navigationBarsWithImePadding()
                } else {
                    // FAB is visible, position is handled by Scaffold based on FAB's position
                    Modifier
                }
            )
        },
        topBar = {
            TopBarContent(
                searchVisible = viewState.searchVisible,
                searchQuery = viewState.searchQuery,
                listState = listState,
                onNavigateToSettings = onNavigateToSettings,
                onRefreshRecipes = onRefreshRecipes,
                onSearchHide = onSearchHide,
                onSearchShow = onSearchShow,
                onSearchQueryChange = onSearchQueryChange
            )
        },
        floatingActionButton = if (fabVisible) {
            {
                val expanded by remember {
                    derivedStateOf {
                        listState.firstVisibleItemIndex == 0
                    }
                }

                ExpandableFloatingActionButton(
                    text = {
                        Text(text = stringResource(R.string.new_recipe))
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.new_recipe),
                            modifier = Modifier.floatingActionButtonSize()
                        )
                    },
                    onClick = onNavigateToRecipeAdd,
                    modifier = Modifier.navigationBarsPadding(),
                    expanded = expanded
                )
            }
        } else {
            {}
        },
    ) { innerPadding ->
        LoadingContent(
            empty = viewState.initialLoad,
            emptyContent = { FullScreenLoading() },
            loading = viewState.loading,
            modifier = Modifier.padding(innerPadding),
            onRefresh = onRefreshRecipes
        ) {
            RecipeListScreenErrorAndContent(
                recipes = recipes,
                isShowingErrors = viewState.errorMessages.isNotEmpty(),
                listState = listState,
                onNavigateToRecipe = onNavigateToRecipe,
                onRefresh = onRefreshRecipes
            )
        }
    }

    // TODO: make reusable
    // Process one error message at a time and show them as Snackbars in the UI
    if (viewState.errorMessages.isNotEmpty()) {
        // Remember the errorMessage to display on the screen
        val errorMessage = remember(viewState.errorMessages) { viewState.errorMessages[0] }

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
private fun TopBarContent(
    searchVisible: Boolean = false,
    searchQuery: String = "",
    listState: LazyListState = rememberLazyListState(),
    onNavigateToSettings: () -> Unit = {},
    onRefreshRecipes: () -> Unit = {},
    onSearchHide: () -> Unit = {},
    onSearchShow: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
) {
    LogCompositions("TopBarContent")
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    TopAppBar(
        title = {
            if (!searchVisible) {
                Text(text = stringResource(R.string.app_name))
            } else {
                val focusRequester = remember { FocusRequester() }

                TextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = {
                        Text(text = stringResource(R.string.search_placeholder))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(R.string.search_placeholder)
                        )
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = stringResource(R.string.search_clear),
                                modifier = Modifier.clickable { onSearchQueryChange("") }
                            )
                        }
                    } else {
                        null
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        autoCorrect = false,
                        imeAction = ImeAction.Search
                    ),
                    colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)
                )

                LaunchedEffect(true) {
                    focusRequester.requestFocus()
                }
            }
        },
        modifier = Modifier.navigationBarsPadding(bottom = false),
        contentPadding = rememberInsetsPaddingValues(
            LocalWindowInsets.current.statusBars,
            applyBottom = false
        ),
        navigationIcon = if (searchVisible) {
            {
                IconButton(onClick = onSearchHide) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        } else {
            null
        },
        actions = {
            if (searchVisible) {
                return@TopAppBar
            }

            var menuExpanded by remember { mutableStateOf(false) }

            IconButton(onClick = onSearchShow) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.search_placeholder)
                )
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null
                    )
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            menuExpanded = false
                            onRefreshRecipes()
                        }
                    ) {
                        Text(text = stringResource(R.string.sync))
                    }
                    DropdownMenuItem(
                        onClick = {
                            menuExpanded = false
                            onNavigateToSettings()
                        }
                    ) {
                        Text(text = stringResource(R.string.settings))
                    }
                }
            }
        },
        backgroundColor = if (!isScrolled) {
            MaterialTheme.colors.background
        } else {
            MaterialTheme.colors.surface
        },
        elevation = if (!isScrolled) 0.dp else 4.dp
    )
}

@Composable
private fun RecipeListScreenErrorAndContent(
    recipes: List<Recipe>,
    isShowingErrors: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    onNavigateToRecipe: (String) -> Unit = {},
    onRefresh: () -> Unit = {},
) {
    LogCompositions("RecipeListScreenErrorAndContent")
    if (recipes.isNotEmpty()) {
        RecipeList(
            recipes = recipes,
            listState = listState,
            onNavigateToRecipe = onNavigateToRecipe
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
        Spacer(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun RecipeList(
    recipes: List<Recipe>,
    listState: LazyListState = rememberLazyListState(),
    onNavigateToRecipe: (String) -> Unit = {},
) {
    LogCompositions("RecipeList")
    val columnsPerRow = when (LocalConfiguration.current.orientation) {
        Configuration.ORIENTATION_LANDSCAPE -> 4
        else -> 2
    }
    val chunkedRecipes = remember(recipes) { recipes.chunked(columnsPerRow) }

    val ime = LocalWindowInsets.current.ime
    val navBars = LocalWindowInsets.current.navigationBars
    val insets = remember(ime, navBars) { derivedWindowInsetsTypeOf(ime, navBars) }

    LazyColumn(
        state = listState,
        contentPadding = rememberInsetsPaddingValues(
            insets = insets,
            applyTop = false,
            additionalStart = 16.dp,
            additionalTop = 16.dp,
            additionalEnd = 16.dp,
            additionalBottom = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            items = chunkedRecipes,
            key = { it[0].id }
        ) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { recipe ->
                    Recipe(
                        recipe = recipe,
                        modifier = Modifier.weight(1f),
                        onNavigateToRecipe = onNavigateToRecipe
                    )
                }

                if (row.size != columnsPerRow) {
                    for (col in 0 until columnsPerRow - row.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun Recipe(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onNavigateToRecipe: (String) -> Unit = {},
) {
    Card(
        modifier = modifier.clickable { onNavigateToRecipe(recipe.id) },
        elevation = 2.dp
    ) {
        Column {
            Image(
                painter = rememberImagePainter(
                    data = recipe.imageUrl ?: R.drawable.ic_food_placeholder,
                    builder = {
                        crossfade(200)
                        error(R.drawable.ic_broken_image)
                    }
                ),
                contentDescription = stringResource(R.string.recipe_image, recipe.title),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .let {
                        if (recipe.imageUrl == null) {
                            it.padding(16.dp)
                        } else {
                            it
                        }
                    },
                alignment = Alignment.TopCenter,
                contentScale = if (recipe.imageUrl != null) ContentScale.Crop else ContentScale.Fit
            )
            Text(
                text = recipe.title,
                modifier = Modifier.padding(8.dp),
                style = MaterialTheme.typography.subtitle1
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
                title = "Koprovka",
                imageUrl = "https://test.api.zradelnik.eu/image/koprova-omacka_60b625cd71cc4b28a638d432?size=640x640&format=webp"
            )
        )
    }
}
