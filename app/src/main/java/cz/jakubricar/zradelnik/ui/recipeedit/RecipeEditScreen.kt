package cz.jakubricar.zradelnik.ui.recipeedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.navigationBarsHeight
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.compose.LogCompositions
import cz.jakubricar.zradelnik.model.RecipeDetail
import cz.jakubricar.zradelnik.network.connectedState
import cz.jakubricar.zradelnik.ui.components.ExpandableFloatingActionButton
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.components.InsetAwareTopAppBar
import cz.jakubricar.zradelnik.ui.components.floatingActionButtonSize
import cz.jakubricar.zradelnik.ui.user.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun RecipeEditScreen(
    viewModel: RecipeEditViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    slug: String? = null,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    onBack: () -> Unit = {}
) {
    LaunchedEffect(slug) {
        if (slug != null) {
            viewModel.getRecipe(slug)
        } else {
            viewModel.setLoading(false)
        }
    }

    val viewState by viewModel.state.collectAsState()
    val userViewState by userViewModel.state.collectAsState()

    RecipeEditScreen(
        viewState = viewState,
        scaffoldState = scaffoldState,
        isNew = slug == null,
        onBack = onBack,
        onRefresh = slug?.let { { viewModel.getRecipe(it) } } ?: {}
    )

    LaunchedEffect(viewState) {
        if (userViewState.loggedInUser == null) {
            onBack()
        }
    }
}

@Composable
fun RecipeEditScreen(
    viewState: RecipeEditViewState,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    isNew: Boolean = true,
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val listState = rememberLazyListState()
    val connected by connectedState()
    val scope = rememberCoroutineScope()
    val onlyOnlineWarningMessage = stringResource(R.string.changes_only_online_warning)

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            InsetAwareTopAppBar(
                title = {
                    Text(
                        text = if (isNew) {
                            stringResource(R.string.new_recipe)
                        } else {
                            viewState.recipe?.title ?: stringResource(R.string.recipe_edit)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = viewState.recipe?.let {
                    {
                        IconButton(
                            onClick = {
                                if (!connected) {
                                    scope.launch {
                                        scaffoldState.snackbarHostState
                                            .showSnackbar(onlyOnlineWarningMessage)
                                    }

                                    return@IconButton
                                }

                                // TODO: Perform delete
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = stringResource(R.string.delete)
                            )
                        }
                    }
                } ?: {},
                backgroundColor = if (listState.firstVisibleItemScrollOffset == 0) {
                    MaterialTheme.colors.background
                } else {
                    MaterialTheme.colors.surface
                },
                elevation = if (listState.firstVisibleItemScrollOffset == 0) 0.dp else 4.dp
            )
        },
        floatingActionButton = if (isNew || viewState.recipe != null) {
            {
                val expanded by remember {
                    derivedStateOf {
                        listState.firstVisibleItemIndex == 0
                    }
                }

                ExpandableFloatingActionButton(
                    text = {
                        Text(text = stringResource(R.string.save))
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = stringResource(R.string.save),
                            modifier = Modifier.floatingActionButtonSize()
                        )
                    },
                    onClick = {
                        if (!connected) {
                            scope.launch {
                                scaffoldState.snackbarHostState
                                    .showSnackbar(onlyOnlineWarningMessage)
                            }

                            return@ExpandableFloatingActionButton
                        }

                        // TODO: Perform save
                    },
                    modifier = Modifier.navigationBarsPadding(),
                    expanded = expanded
                )
            }
        } else {
            {}
        }
    ) { innerPadding ->
        if (viewState.loading) {
            FullScreenLoading()
        } else {
            RecipeScreenErrorAndContent(
                recipe = viewState.recipe,
                modifier = Modifier.padding(innerPadding),
                listState = listState,
                isNew = isNew,
                onRefresh = onRefresh
            )
        }
    }
}

@Composable
private fun RecipeScreenErrorAndContent(
    recipe: RecipeDetail?,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    isNew: Boolean = true,
    onRefresh: () -> Unit = {}
) {
    LogCompositions("RecipeScreenErrorAndContent")
    if (isNew || recipe != null) {
        RecipeEdit(
            recipe = recipe,
            modifier = modifier,
            listState = listState
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.recipe_load_failed),
                modifier = Modifier.padding(bottom = 8.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h6
            )
            Text(
                text = stringResource(R.string.changes_only_online_warning),
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.body2
            )
            Text(
                text = stringResource(R.string.check_internet_connection),
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.caption
            )
            Button(onClick = onRefresh) {
                Text(text = stringResource(R.string.try_again))
            }
        }
    }
}

@Composable
fun RecipeEdit(
    recipe: RecipeDetail?,
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
        item {
            // TODO: Image upload
        }

        item {
            Section(title = stringResource(R.string.basic_info)) {
                // TODO: Basic info (details) edit
            }
        }

        item {
            Section(title = stringResource(R.string.ingredients)) {
                // TODO: Ingredients edit
            }
        }

        item {
            Section(title = stringResource(R.string.directions)) {
                // TODO: Description edit
            }
            Spacer(modifier = Modifier.navigationBarsHeight(16.dp))
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
