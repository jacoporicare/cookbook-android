package cz.jakubricar.zradelnik.ui.recipe

import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.findActivity
import cz.jakubricar.zradelnik.model.RecipeDetail
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.components.InsetAwareTopAppBar
import cz.jakubricar.zradelnik.ui.user.UserViewModel
import cz.jakubricar.zradelnik.ui.user.UserViewState
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch

@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    slug: String,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    onBack: () -> Unit = {},
    onNavigateToRecipeEdit: (String) -> Unit = {},
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
    val userViewState by userViewModel.state.collectAsState()

    if (viewState.keepAwake) {
        val context = LocalContext.current

        DisposableEffect(true) {
            val window = context.findActivity()?.window
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            onDispose {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }
    }

    val scope = rememberCoroutineScope()
    val snackbarKeepAwakeMessage = stringResource(R.string.keep_awake_snackbar_message)

    RecipeScreen(
        viewState = viewState,
        userViewState = userViewState,
        scaffoldState = scaffoldState,
        onBack = onBack,
        onEdit = { onNavigateToRecipeEdit(slug) },
        onKeepAwake = {
            viewModel.toggleKeepAwake()

            if (!viewState.keepAwake) {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(snackbarKeepAwakeMessage)
                }
            }
        }
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
    userViewState: UserViewState,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onKeepAwake: () -> Unit = {}
) {
    val listState = rememberLazyListState()

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(
                hostState = it,
                modifier = Modifier.navigationBarsWithImePadding()
            )
        },
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
                actions = {
                    if (userViewState.loggedInUser != null) {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.edit)
                            )
                        }
                    }

                    IconButton(onClick = onKeepAwake) {
                        Icon(
                            imageVector = if (viewState.keepAwake) {
                                Icons.Filled.LightMode
                            } else {
                                Icons.Outlined.LightMode
                            },
                            contentDescription = stringResource(R.string.keep_awake)
                        )
                    }
                },
                backgroundColor = if (listState.firstVisibleItemScrollOffset == 0) {
                    MaterialTheme.colors.background
                } else {
                    MaterialTheme.colors.surface
                },
                elevation = if (listState.firstVisibleItemScrollOffset == 0) 0.dp else 4.dp
            )
        }
    ) { innerPadding ->
        if (viewState.loading || viewState.recipe == null) {
            FullScreenLoading()
        } else {
            Recipe(
                recipe = viewState.recipe,
                modifier = Modifier.padding(innerPadding),
                listState = listState
            )
        }
    }
}

@Composable
fun Recipe(
    recipe: RecipeDetail,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = rememberInsetsPaddingValues(
            insets = LocalWindowInsets.current.systemBars + LocalWindowInsets.current.ime,
            applyTop = false
        )
    ) {
        recipe.imageUrl?.let { imageUrl ->
            item {
                Image(
                    painter = rememberImagePainter(
                        data = imageUrl,
                        builder = {
                            crossfade(200)
                            error(R.drawable.ic_broken_image)
                        }
                    ),
                    contentDescription = stringResource(R.string.recipe_image, recipe.title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    alignment = Alignment.TopCenter,
                    contentScale = ContentScale.Crop
                )
            }
        }

        if (!recipe.preparationTime.isNullOrEmpty() ||
            !recipe.servingCount.isNullOrEmpty() ||
            !recipe.sideDish.isNullOrEmpty()
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Details(
                    preparationTime = recipe.preparationTime,
                    servingCount = recipe.servingCount,
                    sideDish = recipe.sideDish
                )
            }
        }

        if (recipe.ingredients.isNotEmpty()) {
            item {
                Section(title = stringResource(R.string.ingredients)) {
                    Ingredients(ingredients = recipe.ingredients)
                }
            }
        }

        item {
            Section(title = stringResource(R.string.directions)) {
                MarkdownText(
                    markdown = recipe.directions ?: stringResource(R.string.no_directions)
                )
            }
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp
        ) {
            Box(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }
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
                ingredients.forEachIndexed { index, ingredient ->
                    Text(
                        text = ingredient.amount ?: "",
                        modifier = Modifier.ingredientGroupPadding(index, ingredient),
                        style = MaterialTheme.typography.body2
                    )
                }
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                ingredients.forEachIndexed { index, ingredient ->
                    Text(
                        text = ingredient.amountUnit ?: "",
                        modifier = Modifier.ingredientGroupPadding(index, ingredient),
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
        Column(modifier = Modifier.padding(start = 16.dp)) {
            ingredients.forEachIndexed { index, ingredient ->
                val alpha = if (ingredient.isGroup) ContentAlpha.medium else ContentAlpha.high

                CompositionLocalProvider(LocalContentAlpha provides alpha) {
                    Text(
                        text = ingredient.name,
                        modifier = Modifier.ingredientGroupPadding(index, ingredient),
                        fontWeight = if (ingredient.isGroup) FontWeight.Bold else FontWeight.Normal,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.body2
                    )
                }
            }
        }
    }
}

private fun Modifier.ingredientGroupPadding(
    index: Int,
    ingredient: RecipeDetail.Ingredient
): Modifier {
    if (ingredient.isGroup && index > 0) {
        return padding(top = 16.dp)
    }

    return this
}
