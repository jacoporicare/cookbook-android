package cz.jakubricar.zradelnik.ui.recipeedit

import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.compose.LogCompositions
import cz.jakubricar.zradelnik.model.RecipeEdit.NewImage
import cz.jakubricar.zradelnik.network.connectedState
import cz.jakubricar.zradelnik.ui.ErrorSnackbar
import cz.jakubricar.zradelnik.ui.ErrorState
import cz.jakubricar.zradelnik.ui.TextFieldState
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.login.TextFieldError
import cz.jakubricar.zradelnik.ui.user.UserViewModel
import kotlinx.coroutines.launch

@Composable
fun RecipeEditScreen(
    isInstantPotNewRecipe: Boolean = false,
    viewModel: RecipeEditViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    id: String? = null,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
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
    val formState = remember(viewState.editedRecipe) {
        RecipeEditFormState(viewState.editedRecipe, isInstantPotNewRecipe)
    }

    val authToken = userViewState.authToken
    val context = LocalContext.current

    RecipeEditScreen(
        viewState = viewState,
        formState = formState,
        snackbarHostState = snackbarHostState,
        errorState = viewModel.errorState,
        isNew = id == null,
        onBack = onBack,
        onRefresh = id?.let { { viewModel.getRecipe(it) } } ?: {},
        onSave = {
            if (authToken != null) {
                val newImage = formState.newImageUri.value?.let { uri ->
                    val mimeType = context.contentResolver.getType(uri)
                    val bytes = context.contentResolver.openInputStream(uri)?.buffered()?.use {
                        it.readBytes()
                    }

                    if (mimeType != null && bytes != null) {
                        NewImage(mimeType = mimeType, bytes = bytes)
                    } else {
                        null
                    }
                }

                viewModel.save(authToken, formState, newImage)
            }
        },
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
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    errorState: ErrorState = remember { ErrorState() },
    isNew: Boolean = true,
    onBack: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onSave: () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val failedLoading = !isNew && viewState.editedRecipe == null

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .imePadding()
            )
        },
        topBar = {
            TopBarContent(
                title = viewState.editedRecipe?.title,
                isNew = isNew,
                loading = viewState.loading,
                failedLoading = failedLoading,
                snackbarHostState = snackbarHostState,
                listState = listState,
                onBack = onBack,
                onSave = onSave,
            )
        },
    ) { innerPadding ->
        if (viewState.loading) {
            FullScreenLoading()
        } else {
            RecipeScreenErrorAndContent(
                formState = formState,
                failedLoading = failedLoading,
                modifier = Modifier.padding(innerPadding),
                imageUrl = viewState.editedRecipe?.imageUrl,
                listState = listState,
                onRefresh = onRefresh
            )
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
    title: String?,
    isNew: Boolean,
    loading: Boolean,
    failedLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    listState: LazyListState,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val connected by connectedState()

    val firstVisibleItemScrollOffset = remember {
        derivedStateOf { listState.firstVisibleItemScrollOffset }
    }

    TopAppBar(
        title = {
            Text(
                text = if (isNew) {
                    stringResource(R.string.new_recipe)
                } else {
                    title ?: stringResource(R.string.recipe_edit)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                                snackbarHostState.showSnackbar(onlyOnlineWarningMessage)
                            }

                            return@IconButton
                        }

                        onSave()
                    },
                    enabled = !loading,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = stringResource(R.string.save)
                    )
                }
            }
        },
//        backgroundColor = if (firstVisibleItemScrollOffset.value == 0) {
//            MaterialTheme.colors.background
//        } else {
//            MaterialTheme.colors.surface
//        },
//        elevation = if (firstVisibleItemScrollOffset.value == 0) 0.dp else 4.dp
    )
}

@Composable
private fun RecipeScreenErrorAndContent(
    formState: RecipeEditFormState,
    failedLoading: Boolean,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    listState: LazyListState = rememberLazyListState(),
    onRefresh: () -> Unit = {},
) {
    LogCompositions("RecipeScreenErrorAndContent")
    if (!failedLoading) {
        RecipeEdit(
            formState = formState,
            modifier = modifier,
            imageUrl = imageUrl,
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
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(R.string.changes_only_online_warning),
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.check_internet_connection),
                modifier = Modifier.padding(bottom = 16.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
            Button(onClick = onRefresh) {
                Text(text = stringResource(R.string.try_again))
            }
        }
    }
}

@Composable
fun RecipeEdit(
    formState: RecipeEditFormState,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    listState: LazyListState = rememberLazyListState(),
) {
    val context = LocalContext.current

    val newImage by remember {
        derivedStateOf {
            formState.newImageUri.value?.let {
                if (Build.VERSION.SDK_INT < 28) {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        formState.newImageUri.value = it
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
    ) {
        item {
            if (newImage != null || imageUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(newImage ?: imageUrl)
                        .crossfade(true)
                        .build(),
                    error = painterResource(R.drawable.ic_broken_image),
                    contentDescription = stringResource(
                        R.string.recipe_image,
                        formState.title.value
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .clickable { launcher.launch("image/*") },
                    alignment = Alignment.TopCenter,
                    contentScale = ContentScale.Crop
                )
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) {
                TextButton(onClick = { launcher.launch("image/*") }) {
                    Text(text = stringResource(R.string.pick_photo))
                }
            }
        }
        section(title = { Text(text = stringResource(R.string.basic_info)) }) {
            TextFieldTextState(
                state = formState.title,
                label = { Text(text = stringResource(R.string.recipe_title)) },
                capitalization = KeyboardCapitalization.Sentences,
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .toggleable(
                        role = Role.Switch,
                        value = formState.isForInstantPot,
                        onValueChange = { formState.isForInstantPot = it },
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = stringResource(R.string.instant_pot_recipe))

                Switch(checked = formState.isForInstantPot, onCheckedChange = null)
            }
        }
        section(title = { Text(text = stringResource(R.string.ingredients)) }) {
            formState.ingredients.forEachIndexed { index, ingredientFormState ->
                if (index > 0) {
                    HorizontalDivider()
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextFieldTextState(
                            state = ingredientFormState.name,
                            modifier = Modifier.weight(1f),
                            label = { Text(text = stringResource(R.string.ingredient_name)) },
                        )

                        IconButton(
                            onClick = {
                                formState.ingredients =
                                    formState.ingredients - ingredientFormState
                            },
                            modifier = Modifier.padding(vertical = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
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
                            if (ingredientFormState.isGroup.value) {
                                Text(
                                    text = stringResource(R.string.ingredient_group),
                                    modifier = Modifier.weight(1f),
                                )
                            }

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
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = {
                    formState.ingredients =
                        formState.ingredients + RecipeEditFormState.IngredientFormState()
                }) {
                    Text(text = stringResource(R.string.add_ingredient))
                }
            }
        }
        section(title = { Text(text = stringResource(R.string.directions)) }) {
            TextFieldTextState(
                state = formState.directions,
                label = { Text(text = stringResource(R.string.directions)) },
                singleLine = false
            )
        }
        item {
            Spacer(
                modifier = Modifier
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                    .padding(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.section(
    title: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    item {
        Spacer(modifier = Modifier.height(16.dp))
    }
    stickyHeader {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            ProvideTextStyle(MaterialTheme.typography.headlineSmall) {
                title()
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        HorizontalDivider()
    }
    item {
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
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
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
) {
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
                capitalization = capitalization,
                imeAction = if (singleLine) ImeAction.Next else ImeAction.None,
                keyboardType = keyboardType
            ),
            singleLine = singleLine,
            maxLines = maxLines
        )

        state.getError()?.let { error ->
            TextFieldError(textError = stringResource(error))
        }
    }
}
