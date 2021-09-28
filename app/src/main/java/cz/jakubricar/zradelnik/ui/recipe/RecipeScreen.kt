package cz.jakubricar.zradelnik.ui.recipe

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import cz.jakubricar.zradelnik.model.RecipeDetail

@Composable
fun RecipeScreen(
    viewModel: RecipeViewModel = hiltViewModel(),
    slug: String,
    onBack: () -> Unit
) {
    LaunchedEffect(slug) {
        // The app opened for the first time, navigate to the list to fetch recipes
        if (viewModel.initialSync()) {
            onBack()
        } else {
            viewModel.getRecipe(slug)
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    if (uiState.recipe != null) {
        RecipeScreen(
            recipe = uiState.recipe!!,
            onBack = onBack
        )
    }

    LaunchedEffect(uiState) {
        if (uiState.failedLoading) {
            onBack()
        }
    }
}

@Composable
fun RecipeScreen(
    recipe: RecipeDetail,
    onBack: () -> Unit
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Text(text = recipe.title)
            Button(onClick = onBack) {
                Text(text = "< Back")
            }
        }
    }
}
