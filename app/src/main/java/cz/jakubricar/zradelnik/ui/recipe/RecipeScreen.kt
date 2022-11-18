package cz.jakubricar.zradelnik.ui.recipe

import android.app.DatePickerDialog
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.AlertDialog
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.google.accompanist.insets.derivedWindowInsetsTypeOf
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.findActivity
import cz.jakubricar.zradelnik.model.Recipe
import cz.jakubricar.zradelnik.network.connectedState
import cz.jakubricar.zradelnik.ui.ErrorSnackbar
import cz.jakubricar.zradelnik.ui.ErrorState
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.user.UserViewModel
import cz.jakubricar.zradelnik.ui.user.UserViewState
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    id: String,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    onBack: () -> Unit = {},
    onNavigateToRecipeEdit: (String) -> Unit = {},
) {
    LaunchedEffect(id) {
        viewModel.getRecipe(id)
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

    val authToken = userViewState.authToken

    RecipeScreen(
        viewState = viewState,
        userViewState = userViewState,
        scaffoldState = scaffoldState,
        errorState = viewModel.errorState,
        onBack = onBack,
        onEdit = { onNavigateToRecipeEdit(id) },
        onDelete = {
            if (authToken != null) {
                viewModel.deleteRecipe(authToken, id)
            }
        },
        onKeepAwake = {
            viewModel.toggleKeepAwake()

            if (!viewState.keepAwake) {
                scope.launch {
                    scaffoldState.snackbarHostState.showSnackbar(snackbarKeepAwakeMessage)
                }
            }
        },
        onCooked = { date ->
            if (authToken != null) {
                viewModel.recipeCooked(authToken, id, date)
            }
        }
    )

    LaunchedEffect(viewState) {
        if (viewState.failedLoading || viewState.navigateToList) {
            onBack()
        }
    }
}

@Composable
fun RecipeScreen(
    viewState: RecipeViewState,
    userViewState: UserViewState,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    errorState: ErrorState = remember { ErrorState() },
    onBack: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onKeepAwake: () -> Unit = {},
    onCooked: (OffsetDateTime) -> Unit = {},
) {
    val listState = rememberLazyListState()
    var deleteRecipeDialogOpened by remember { mutableStateOf(false) }
    var recipeCookedDialogOpened by remember { mutableStateOf(false) }
    val title = viewState.recipe?.title
    val keepAwake = viewState.keepAwake
    val isUserLoggedIn = userViewState.loggedInUser != null
    val context = LocalContext.current

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(
                hostState = it,
                modifier = Modifier.navigationBarsWithImePadding()
            )
        },
        topBar = {
            TopBarContent(
                title = title,
                keepAwake = keepAwake,
                isUserLoggedIn = isUserLoggedIn,
                scaffoldState = scaffoldState,
                listState = listState,
                onBack = onBack,
                onDelete = { deleteRecipeDialogOpened = true },
                onEdit = onEdit,
                onKeepAwake = onKeepAwake,
                onCooked = { recipeCookedDialogOpened = true },
            )
        },
    ) { innerPadding ->
        when {
            viewState.loading -> {
                FullScreenLoading()
            }
            viewState.recipe == null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.recipe_not_found),
                        style = MaterialTheme.typography.h5
                    )
                }
            }
            else -> {
                Recipe(
                    recipe = viewState.recipe,
                    modifier = Modifier.padding(innerPadding),
                    listState = listState
                )
            }
        }
    }

    if (deleteRecipeDialogOpened) {
        AlertDialog(
            onDismissRequest = { deleteRecipeDialogOpened = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteRecipeDialogOpened = false
                        onDelete()
                    }
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteRecipeDialogOpened = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
            title = { Text(text = stringResource(R.string.delete_recipe_title)) },
            text = {
                Text(
                    text = stringResource(
                        R.string.delete_recipe_text,
                        viewState.recipe?.title ?: ""
                    ),
                )
            },
        )
    }

    if (recipeCookedDialogOpened) {
        AlertDialog(
            onDismissRequest = { recipeCookedDialogOpened = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        recipeCookedDialogOpened = false

                        val now = OffsetDateTime.now()
                        val dialog = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                onCooked(
                                    OffsetDateTime.of(year,
                                        month + 1,
                                        dayOfMonth,
                                        0,
                                        0,
                                        0,
                                        0,
                                        ZoneOffset.UTC
                                    )
                                )
                            },
                            now.year,
                            now.monthValue - 1,
                            now.dayOfMonth,
                        )

                        dialog.show()
                    }
                ) {
                    Text(text = stringResource(R.string.recipe_cooked_pick_date_button))
                }
            },
            dismissButton = {
                TextButton(onClick = { recipeCookedDialogOpened = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
            title = { Text(text = stringResource(R.string.recipe_cooked_title)) },
            text = { Text(text = stringResource(R.string.recipe_cooked_text)) },
        )
    }

    ErrorSnackbar(
        errorState = errorState,
        scaffoldState = scaffoldState,
    )
}

@Composable
private fun TopBarContent(
    title: String?,
    keepAwake: Boolean,
    isUserLoggedIn: Boolean,
    scaffoldState: ScaffoldState,
    listState: LazyListState,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onKeepAwake: () -> Unit,
    onCooked: () -> Unit,
) {
    val firstVisibleItemScrollOffset = remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset }
    }

    TopAppBar(
        title = {
            Text(text = title ?: stringResource(R.string.recipe))
        },
        modifier = Modifier.navigationBarsPadding(bottom = false),
        contentPadding = rememberInsetsPaddingValues(
            LocalWindowInsets.current.statusBars,
            applyBottom = false
        ),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        actions = {
            if (isUserLoggedIn) {
                IconButton(onClick = onCooked) {
                    Icon(
                        imageVector = Icons.Default.DinnerDining,
                        contentDescription = stringResource(R.string.cooked)
                    )
                }
            }

            IconButton(onClick = onKeepAwake) {
                Icon(
                    imageVector = if (keepAwake) {
                        Icons.Filled.LightMode
                    } else {
                        Icons.Outlined.LightMode
                    },
                    contentDescription = stringResource(R.string.keep_awake)
                )
            }

            if (isUserLoggedIn) {
                val connected by connectedState()
                val scope = rememberCoroutineScope()
                val onlyOnlineWarningMessage =
                    stringResource(R.string.changes_only_online_warning)
                var menuExpanded by remember { mutableStateOf(false) }

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
                                onEdit()
                            }
                        ) {
                            Text(text = stringResource(R.string.edit))
                        }
                        DropdownMenuItem(
                            onClick = {
                                menuExpanded = false

                                if (!connected) {
                                    scope.launch {
                                        scaffoldState.snackbarHostState
                                            .showSnackbar(onlyOnlineWarningMessage)
                                    }

                                    return@DropdownMenuItem
                                }

                                onDelete()
                            }
                        ) {
                            Text(text = stringResource(R.string.delete))
                        }
                    }
                }
            }
        },
        backgroundColor = if (firstVisibleItemScrollOffset.value == 0) {
            MaterialTheme.colors.background
        } else {
            MaterialTheme.colors.surface
        },
        elevation = if (firstVisibleItemScrollOffset.value == 0) 0.dp else 4.dp
    )
}

@Composable
fun Recipe(
    recipe: Recipe,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val ime = LocalWindowInsets.current.ime
    val navBars = LocalWindowInsets.current.navigationBars
    val insets = remember(ime, navBars) { derivedWindowInsetsTypeOf(ime, navBars) }
    var instantPotInfoVisible by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = rememberInsetsPaddingValues(
            insets = insets,
            applyTop = false,
            applyBottom = false,
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

        if (recipe.isForInstantPot) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { instantPotInfoVisible = !instantPotInfoVisible }
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(text = stringResource(R.string.instant_pot_recipe))
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = stringResource(R.string.more_info)
                            )
                        }

                        AnimatedVisibility(visible = instantPotInfoVisible) {
                            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                                Text(text = stringResource(R.string.instant_pot_recipe_info))
                            }
                        }
                    }
                }
            }
        }

        recipe.cookedHistory.lastOrNull()?.let {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                CookedHistory(cooked = it)
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
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Spacer(modifier = Modifier.height(16.dp))
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.padding(8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun CookedHistory(
    modifier: Modifier = Modifier,
    cooked: Recipe.Cooked,
) {
    DetailItem(
        modifier = modifier.padding(horizontal = 16.dp),
        label = "${stringResource(R.string.cooked_last_time)}:",
        value = "${cooked.date} (${cooked.userDisplayName})"
    )

}

@Composable
private fun Details(
    modifier: Modifier = Modifier,
    preparationTime: String? = null,
    servingCount: String? = null,
    sideDish: String? = null,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        preparationTime?.let {
            DetailItem(
                label = "${stringResource(R.string.preparation_time)}:",
                value = it
            )
        }
        servingCount?.let {
            DetailItem(
                label = "${stringResource(R.string.serving_count)}:",
                value = it
            )
        }
        sideDish?.let {
            DetailItem(
                label = "${stringResource(R.string.side_dish)}:",
                value = it
            )
        }
    }
}

@Composable
private fun DetailItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
    ingredients: List<Recipe.Ingredient>,
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
    ingredient: Recipe.Ingredient,
): Modifier {
    if (ingredient.isGroup && index > 0) {
        return padding(top = 16.dp)
    }

    return this
}
