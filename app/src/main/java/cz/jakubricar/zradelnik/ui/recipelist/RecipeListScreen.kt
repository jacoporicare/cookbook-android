package cz.jakubricar.zradelnik.ui.recipelist

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme

@Composable
fun RecipeListScreen(
    navigateToRecipe: (String) -> Unit,
    viewModel: RecipeListViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    val recipes =
        remember(uiState.recipes, searchQuery) { filterRecipes(uiState.recipes, searchQuery) }

    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.systemBars,
                applyTop = true,
                applyBottom = true,
            )
        ) {
            items(recipes, { it.id }) {
                Button(onClick = { navigateToRecipe(it.slug) }) {
                    Recipe(it.title)
                }
            }
        }
    }
}

@Composable
fun Recipe(title: String) {
    Text(text = title)
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ZradelnikTheme {
        Recipe("Android")
    }
}
