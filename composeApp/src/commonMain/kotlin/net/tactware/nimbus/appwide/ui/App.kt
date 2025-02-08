package net.tactware.nimbus.appwide.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.tactware.nimbus.appwide.ui.main.MainViewInteractions
import net.tactware.nimbus.appwide.ui.main.MainViewModel
import net.tactware.nimbus.projects.ui.ShowProjects
import net.tactware.nimbus.appwide.ui.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    AppTheme {
        val mainViewModel = koinViewModel<MainViewModel>()
        val state = mainViewModel.uiState.collectAsState().value
        Scaffold { innerPadding ->
            Box(Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                when (state) {
                    MainViewModel.UiState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Error")
                            Button(onClick = { mainViewModel.onInteraction(MainViewInteractions.RetryLoadProject) }) {
                                Text("Retry")
                            }
                        }
                    }

                    is MainViewModel.UiState.LoadedProjects -> {
                        ShowProjects(projects = state.projects)
                    }

                    MainViewModel.UiState.Loading -> {
                        Column(Modifier.fillMaxWidth(.5f), horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(Modifier.size(100.dp))
                            Text("Loading...", style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                }
            }
        }
    }
}


