package cz.jakubricar.zradelnik.ui.recipelist

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.compose.LogCompositions
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.ui.BottomBarNavigation
import cz.jakubricar.zradelnik.ui.ErrorSnackbar
import cz.jakubricar.zradelnik.ui.ErrorState
import cz.jakubricar.zradelnik.ui.components.ExpandableFloatingActionButton
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.components.floatingActionButtonSize
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme
import cz.jakubricar.zradelnik.ui.user.UserViewModel
import cz.jakubricar.zradelnik.ui.user.UserViewState

@Composable
fun RecipeListScreen(
    navController: NavController,
    isInstantPotScreen: Boolean = false,
    viewModel: RecipeListViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onNavigateToRecipe: (String) -> Unit = {},
    onNavigateToRecipeAdd: (Boolean) -> Unit = {},
) {
    LogCompositions("RecipeListScreen")
    val viewState by viewModel.state.collectAsState()
    val userViewState by userViewModel.state.collectAsState()

    BackHandler(viewState.searchVisible) {
        viewModel.hideSearch()
    }

    RecipeListScreen(
        navController = navController,
        isInstantPotScreen = isInstantPotScreen,
        viewState = viewState,
        userViewState = userViewState,
        snackbarHostState = snackbarHostState,
        errorState = viewModel.errorState,
        onNavigateToRecipe = onNavigateToRecipe,
        onNavigateToRecipeAdd = onNavigateToRecipeAdd,
        onRefreshRecipes = { viewModel.refreshRecipes() },
        onSearchShow = { viewModel.showSearch() },
        onSearchHide = { viewModel.hideSearch() },
        onSearchQueryChange = { viewModel.setSearchQuery(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    navController: NavController,
    isInstantPotScreen: Boolean = false,
    viewState: RecipeListViewState,
    userViewState: UserViewState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    errorState: ErrorState = remember { ErrorState() },
    onNavigateToRecipe: (String) -> Unit = {},
    onNavigateToRecipeAdd: (Boolean) -> Unit = {},
    onRefreshRecipes: () -> Unit = {},
    onSearchShow: () -> Unit = {},
    onSearchHide: () -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
) {
    LogCompositions("RecipeListScreenStateless")
    val recipes = remember(viewState.recipes, viewState.searchQuery) {
        filterAndSortRecipes(
            if (isInstantPotScreen) viewState.instantPotRecipes else viewState.recipes,
            viewState.searchQuery
        )
    }
    val listState = rememberLazyListState()

    val fabVisible = userViewState.loggedInUser != null

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = if (!fabVisible) {
                    Modifier
                        .navigationBarsPadding()
                        .imePadding()
                } else {
                    // FAB is visible, position is handled by Scaffold based on FAB's position
                    Modifier
                }
            )
        },
        topBar = {
            TopBarContent(
                isInstantPotScreen = isInstantPotScreen,
                searchVisible = viewState.searchVisible,
                searchQuery = viewState.searchQuery,
                listState = listState,
                onRefreshRecipes = onRefreshRecipes,
                onSearchHide = onSearchHide,
                onSearchShow = onSearchShow,
                onSearchQueryChange = onSearchQueryChange
            )
        },
        bottomBar = {
            BottomBarNavigation(navController = navController)
        },
        floatingActionButton = if (fabVisible) {
            {
                val expanded by remember {
                    derivedStateOf {
                        listState.firstVisibleItemIndex == 0
                    }
                }

                val textId =
                    if (isInstantPotScreen) R.string.new_instant_pot_recipe else R.string.new_recipe

                ExpandableFloatingActionButton(
                    text = {
                        Text(text = stringResource(textId))
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(textId),
                            modifier = Modifier.floatingActionButtonSize()
                        )
                    },
                    onClick = { onNavigateToRecipeAdd(isInstantPotScreen) },
                    expanded = expanded
                )
            }
        } else {
            {}
        },
    ) { innerPadding ->
        when {
            viewState.initialLoad -> {
                FullScreenLoading()
            }

            else -> {
                Box(
                    modifier = Modifier
                        .pullToRefresh(
                            isRefreshing = viewState.loading,
                            state = rememberPullToRefreshState(),
                            onRefresh = onRefreshRecipes
                        )
                        .consumeWindowInsets(innerPadding)
                        .padding(innerPadding)
                ) {
                    when {
                        recipes.isNotEmpty() -> {
                            RecipeList(
                                recipes = recipes,
                                listState = listState,
                                onNavigateToRecipe = onNavigateToRecipe
                            )
                        }

                        errorState.errorMessages.isEmpty() -> {
                            // if there are no posts, and no error, let the user refresh manually
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(R.string.no_recipes),
                                    modifier = Modifier.padding(bottom = 8.dp),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Button(onClick = onRefreshRecipes) {
                                    Text(text = stringResource(R.string.try_again))
                                }
                            }
                        }

                        else -> {
                            // there's currently an error showing, don't show any content
                            Spacer(modifier = Modifier.fillMaxSize())
                        }
                    }
                }
            }
        }
    }

    ErrorSnackbar(
        errorState = errorState,
        snackbarHostState = snackbarHostState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBarContent(
    isInstantPotScreen: Boolean = false,
    searchVisible: Boolean = false,
    searchQuery: String = "",
    listState: LazyListState = rememberLazyListState(),
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
                Text(
                    text = stringResource(if (isInstantPotScreen) R.string.instant_pot_recipes else R.string.app_name),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
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
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Search
                    ),
//                    colors = TextFieldDefaults.colors(backgroundColor = Color.Transparent)
                )

                LaunchedEffect(true) {
                    focusRequester.requestFocus()
                }
            }
        },
        navigationIcon = {
            if (!searchVisible) {
                return@TopAppBar
            }

            IconButton(onClick = onSearchHide) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
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
                        text = { Text(text = stringResource(R.string.refresh)) },
                        onClick = {
                            menuExpanded = false
                            onRefreshRecipes()
                        }
                    )
                }
            }
        },
//        backgroundColor = if (!isScrolled) {
//            MaterialTheme.colors.background
//        } else {
//            MaterialTheme.colors.surface
//        },
//        elevation = if (!isScrolled) 0.dp else 4.dp
    )
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

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
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
@OptIn(ExperimentalMaterial3Api::class)
fun Recipe(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    onNavigateToRecipe: (String) -> Unit = {},
) {
    Card(
        onClick = { onNavigateToRecipe(recipe.id) },
        modifier = modifier
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(recipe.imageUrl ?: R.drawable.ic_food_placeholder)
                    .crossfade(true)
//                    .error(R.drawable.ic_broken_image.toDrawable().asImage())
                    .build(),
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
                style = MaterialTheme.typography.headlineSmall
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
                imageUrl = "https://api-test.zradelnik.eu/image/koprova-omacka_60b625cd71cc4b28a638d432?size=640x640&format=webp",
                directions = null,
                ingredients = emptyList(),
                preparationTime = null,
                servingCount = null,
                sideDish = null,
                cookedHistory = emptyList(),
                tags = emptyList(),
            )
        )
    }
}
