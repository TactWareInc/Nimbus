package net.tactware.nimbus.projects.ui.specific

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import net.tactware.nimbus.projects.ui.ExposedSearchMenu
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * UI component for displaying repositories for a specific project.
 */
@Composable
fun RepositoriesUi(projectIdentifier: ProjectIdentifier) {
    val viewModel = koinViewModel<RepositoriesViewModel> { parametersOf(projectIdentifier) }

    // State for snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // Collect states from view model
    val repos = viewModel.projectGitRepos.collectAsState().value
    val searchText = viewModel.searchText.collectAsState().value
    val isCloning = viewModel.isCloning.collectAsState().value
    val cloningRepoId = viewModel.cloningRepoId.collectAsState().value
    val cloningMessage = viewModel.cloningMessage.collectAsState().value
    val cloningResult = viewModel.cloningResult.collectAsState().value

    // Show snackbar when cloning message changes
    LaunchedEffect(cloningMessage) {
        cloningMessage?.let {
            snackbarHostState.showSnackbar(it)
            // Clear the message after showing it
            viewModel.clearCloningMessage()
        }
    }

    // State for alert dialog
    var showResultDialog by remember { mutableStateOf(false) }

    // Show alert dialog when cloning result changes
    LaunchedEffect(cloningResult) {
        if (cloningResult != null) {
            showResultDialog = true
        }
    }

    // Alert dialog for cloning result
    if (showResultDialog && cloningResult != null) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                viewModel.clearCloningResult()
            },
            title = { Text(if (cloningResult.isSuccess) "Success" else "Error") },
            text = {
                Text(
                    if (cloningResult.isSuccess) "Repository cloned successfully."
                    else "Failed to clone repository: ${cloningResult.exceptionOrNull()?.message ?: "Unknown error"}"
                )
            },
            confirmButton = {
                Button(onClick = {
                    showResultDialog = false
                    viewModel.clearCloningResult()
                }) {
                    Text("OK")
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            // Search functionality
            var searchTextState by remember { mutableStateOf(searchText) }
            val test = listOf("Test", "Test2", "Test3")
            val filteredList = derivedStateOf {
                test.filter { it.contains(searchTextState, ignoreCase = true) }
            }
            var selection by remember { mutableStateOf("") }
            var expanded by remember { mutableStateOf(false) }

            ExposedSearchMenu(
                expanded,
                onExpandedChange = { expanded = it },
                filteredList.value,
                itemContent = {
                    DropdownMenuItem(text = {
                        Text(it)
                    }, onClick = {
                        selection = it
                        expanded = false
                    })
                },
                searchContent = {
                    TextField(
                        value = searchTextState,
                        onValueChange = {
                            searchTextState = it
                            viewModel.updateSearchText(it)
                        },
                        label = { Text("Search Repositories") }
                    )
                },
                displayContent = {
                    OutlinedTextField(
                        value = selection,
                        onValueChange = { },
                        label = {
                            Text("Repository")
                        },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
                        readOnly = true
                    )
                }
            )

            // List of repositories
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(repos) { repo ->
                    ListItem(
                        leadingContent = {
                            if (cloningRepoId == repo.id) {
                                CircularProgressIndicator()
                            }
                        },
                        headlineContent = {
                            Text(repo.name)
                        },
                        supportingContent = {
                            Column {
                                Text(repo.url)
                                // Note: After rebuilding, the GitRepo objects will have isCloned and clonePath fields
                                if (repo.isCloned) {
                                    Text("Cloned to: ${repo.clonePath}")
                                }
                            }
                        },
                        trailingContent = {
                            // Note: After rebuilding, the GitRepo objects will have isCloned field
                            IconButton(
                                onClick = { viewModel.cloneRepository(repo) },
                                enabled = !isCloning && !repo.isCloned
                            ) {
                                Icon(
                                    Icons.Default.Add, 
                                    contentDescription = if (repo.isCloned) "Repository Already Cloned" else "Clone Repository"
                                )
                            }
                        }
                    )
                }
            }
        }

        // No longer need a full-screen loading indicator as it's now shown in the list item

        // Snackbar host at the bottom
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}
