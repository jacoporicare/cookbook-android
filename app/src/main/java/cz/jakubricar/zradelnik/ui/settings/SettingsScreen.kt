package cz.jakubricar.zradelnik.ui.settings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.insets.navigationBarsHeight
import cz.jakubricar.zradelnik.R
import cz.jakubricar.zradelnik.ui.components.FullScreenLoading
import cz.jakubricar.zradelnik.ui.components.InsetAwareTopAppBar

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreen(
        settings = uiState.settings,
        loading = uiState.loading,
        onBack = onBack
    )
}

@Composable
fun SettingsScreen(
    settings: Any?,
    loading: Boolean,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            InsetAwareTopAppBar(
                title = {
                    Text(text = stringResource(R.string.settings))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                backgroundColor = if (scrollState.value == 0) {
                    MaterialTheme.colors.background
                } else {
                    MaterialTheme.colors.surface
                },
                elevation = if (scrollState.value == 0) 0.dp else 4.dp
            )
        }
    ) { innerPadding ->
        if (loading || settings == null) {
            FullScreenLoading()
        } else {
            Settings(
                settings = settings,
                modifier = Modifier.padding(innerPadding),
                scrollState = scrollState
            )
        }
    }
}

@Composable
fun Settings(
    settings: Any,
    modifier: Modifier = Modifier,
    scrollState: ScrollState
) {
    Column(modifier = modifier.verticalScroll(scrollState)) {
        Spacer(modifier = Modifier.navigationBarsHeight())
    }
}
