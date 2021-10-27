package cz.jakubricar.zradelnik.ui.recipeedit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.derivedWindowInsetsTypeOf
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.navigationBarsWithImePadding
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import com.google.accompanist.insets.ui.TopAppBar
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.compose.LogCompositions
import cz.jakubricar.zradelnik.network.connectedState
import cz.jakubricar.zradelnik.ui.TextFieldState
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.login.TextFieldError
import cz.jakubricar.zradelnik.ui.user.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun RecipeEditScreen(
    viewModel: RecipeEditViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    id: String? = null,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    onBack: () -> Unit = {},
    onNavigateToRecipe: (String) -> Unit = {},
) {
    LaunchedEffect(id) {
        if (id != null) {
            viewModel.getRecipe(id)
        } else {
            viewModel.setLoading(false)
        }
    }

    val viewState by viewModel.state.collectAsState()
    val userViewState by userViewModel.state.collectAsState()
    val formState = remember(viewState.editedRecipe) { RecipeEditFormState(viewState.editedRecipe) }

    val authToken = userViewState.authToken

    RecipeEditScreen(
        viewState = viewState,
        formState = formState,
        scaffoldState = scaffoldState,
        isNew = id == null,
        onBack = onBack,
        onRefresh = id?.let { { viewModel.getRecipe(it) } } ?: {},
        onSave = {
            if (authToken != null) {
                viewModel.save(authToken, formState)
            }
        },
        onErrorDismiss = { viewModel.errorShown(it) },
    )

    LaunchedEffect(userViewState.loggedInUser) {
        if (userViewState.loggedInUser == null) {
            onBack()
        }
    }

    val navigateToRecipeId = viewState.navigateToRecipeId
    LaunchedEffect(navigateToRecipeId) {
        if (navigateToRecipeId != null) {
            onNavigateToRecipe(navigateToRecipeId)
        }
    }
}

@Composable
fun RecipeEditScreen(
    viewState: RecipeEditViewState,
    formState: RecipeEditFormState,
    scaffoldState: ScaffoldState = rememberScaffoldState(),
    isNew: Boolean = true,
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onSave: () -> Unit = {},
    onErrorDismiss: (Long) -> Unit = {},
) {
    val listState = rememberLazyListState()
    val connected by connectedState()
    val scope = rememberCoroutineScope()
    val failedLoading = !isNew && viewState.editedRecipe == null

    Scaffold(
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(
                hostState = it,
                modifier = Modifier.navigationBarsWithImePadding()
            )
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNew) {
                            stringResource(R.string.new_recipe)
                        } else {
                            viewState.editedRecipe?.title ?: stringResource(R.string.recipe_edit)
                        }
                    )
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
                    if (!failedLoading) {
                        val onlyOnlineWarningMessage =
                            stringResource(R.string.changes_only_online_warning)

                        IconButton(
                            onClick = {
                                if (!connected) {
                                    scope.launch {
                                        scaffoldState.snackbarHostState
                                            .showSnackbar(onlyOnlineWarningMessage)
                                    }

                                    return@IconButton
                                }

                                onSave()
                            },
                            enabled = !viewState.loading,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Save,
                                contentDescription = stringResource(R.string.save)
                            )
                        }
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
        if (viewState.loading) {
            FullScreenLoading()
        } else {
            RecipeScreenErrorAndContent(
                formState = formState,
                failedLoading = failedLoading,
                modifier = Modifier.padding(innerPadding),
                listState = listState,
                onRefresh = onRefresh
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

        // If onErrorDismiss change while the LaunchedEffect is running,
        // don't restart the effect and use the latest lambda values.
        val onErrorDismissState by rememberUpdatedState(onErrorDismiss)

        LaunchedEffect(errorMessage.id, scaffoldState) {
            scaffoldState.snackbarHostState.showSnackbar(
                message = errorMessageText,
                actionLabel = if (errorMessage.tryAgain) retryMessageText else null
            )

            // Once the message is displayed and dismissed, notify the ViewModel
            onErrorDismissState(errorMessage.id)
        }
    }
}

@Composable
private fun RecipeScreenErrorAndContent(
    formState: RecipeEditFormState,
    failedLoading: Boolean,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onRefresh: () -> Unit = {},
) {
    LogCompositions("RecipeScreenErrorAndContent")
    if (!failedLoading) {
        RecipeEdit(
            formState = formState,
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecipeEdit(
    formState: RecipeEditFormState,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val ime = LocalWindowInsets.current.ime
    val navBars = LocalWindowInsets.current.navigationBars
    val insets = remember(ime, navBars) { derivedWindowInsetsTypeOf(ime, navBars) }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = rememberInsetsPaddingValues(
            insets = insets,
            applyTop = false
        )
    ) {
        item {
            // TODO: Image upload
        }

        item {
            Section(title = stringResource(R.string.basic_info)) {
                TextFieldTextState(
                    state = formState.title,
                    label = { Text(text = stringResource(R.string.recipe_title)) }
                )
                TextFieldTextState(
                    state = formState.preparationTime,
                    label = { Text(text = stringResource(R.string.preparation_time)) },
                    trailingIcon = { Text(text = "min") },
                    keyboardType = KeyboardType.Number
                )
                TextFieldTextState(
                    state = formState.servingCount,
                    label = { Text(text = stringResource(R.string.serving_count)) },
                    keyboardType = KeyboardType.Number
                )
                TextFieldTextState(
                    state = formState.sideDish,
                    label = { Text(text = stringResource(R.string.side_dish)) }
                )
            }
        }

        item {
            Section(title = stringResource(R.string.ingredients)) {
                formState.ingredients.forEachIndexed { index, ingredientFormState ->
                    if (index > 0) {
                        Divider()
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextFieldTextState(
                                state = ingredientFormState.name,
                                modifier = Modifier.weight(1f),
                                label = { Text(text = stringResource(R.string.ingredient_name)) },
                                onValueChange = {
                                    val last = formState.ingredients.last()
                                    val secondLast =
                                        formState.ingredients.elementAtOrNull(formState.ingredients.lastIndex - 1)

                                    if (last.name.value != "") {
                                        formState.ingredients =
                                            formState.ingredients + RecipeEditFormState.IngredientFormState()
                                    } else if (last.name.value == "" && secondLast?.name?.value == "") {
                                        formState.ingredients = formState.ingredients.dropLast(1)
                                    }
                                }
                            )

                            if (index < formState.ingredients.size - 1) {
                                IconButton(
                                    onClick = {
                                        formState.ingredients =
                                            formState.ingredients - ingredientFormState
                                    },
                                    modifier = Modifier.padding(vertical = 4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = null,
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(48.dp))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (!ingredientFormState.isGroup.value) {
                                TextFieldTextState(
                                    state = ingredientFormState.amount,
                                    modifier = Modifier.weight(2f),
                                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Right),
                                    label = { Text(text = stringResource(R.string.ingredient_amount)) },
                                    keyboardType = KeyboardType.Number
                                )
                                TextFieldTextState(
                                    state = ingredientFormState.amountUnit,
                                    modifier = Modifier.weight(3f),
                                    label = { Text(text = stringResource(R.string.ingredient_amount_unit)) }
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    onClick = {
                                        ingredientFormState.isGroup.value =
                                            !ingredientFormState.isGroup.value
                                    },
                                    modifier = Modifier.padding(vertical = 4.dp),
                                ) {
                                    Icon(
                                        imageVector = if (!ingredientFormState.isGroup.value) {
                                            Icons.Outlined.Layers
                                        } else {
                                            Icons.Filled.Layers
                                        },
                                        contentDescription = stringResource(R.string.ingredient_group),
                                    )
                                }

                                if (ingredientFormState.isGroup.value) {
                                    Text(text = stringResource(R.string.ingredient_group))
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Section(title = stringResource(R.string.directions)) {
                TextFieldTextState(
                    state = formState.directions,
                    label = { Text(text = stringResource(R.string.directions)) },
                    singleLine = false
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Spacer(modifier = Modifier.height(24.dp))
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.h6
        )
        Spacer(modifier = Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            content()
        }
    }
}

@Composable
private fun TextFieldTextState(
    state: TextFieldState,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit = {},
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = modifier) {
        TextField(
            value = state.value,
            onValueChange = {
                state.value = it
                onValueChange(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    state.onFocusChange(focusState.isFocused)

                    if (!focusState.isFocused) {
                        state.enableShowErrors()
                    }
                },
            textStyle = textStyle,
            label = label,
            trailingIcon = trailingIcon,
            isError = state.showErrors(),
            keyboardOptions = KeyboardOptions(
                imeAction = if (singleLine) ImeAction.Next else ImeAction.None,
                keyboardType = keyboardType
            ),
            // TODO: keyboardActions can be removed once FocusDirection.Next is implemented.
            //  TextField automatically calls moveFocus(FocusDirection.Next) when
            //  imeAction is Next.
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            ),
            singleLine = singleLine,
            maxLines = maxLines
        )

        state.getError()?.let { error ->
            TextFieldError(textError = stringResource(error))
        }
    }
}
