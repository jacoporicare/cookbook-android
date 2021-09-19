package cz.jakubricar.zradelnik.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import cz.jakubricar.zradelnik.ui.theme.ZradelnikTheme

@Composable
fun HomeScreen(
    navigateToRecipe: (String) -> Unit
) {
    Surface(
        color = MaterialTheme.colors.background,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            for (i in 1..100) {
                Button(onClick = { navigateToRecipe(i.toString()) }) {
                    Greeting("Android $i")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ZradelnikTheme {
        Greeting("Android")
    }
}
