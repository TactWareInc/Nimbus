package net.tactware.nimbus.projects.ui.specific

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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

    // Collect states from view model
    val repos = viewModel.projectGitRepos.collectAsState().value
    val searchText = viewModel.searchText.collectAsState().value
    val isCloning = viewModel.isCloning.collectAsState().value
    val cloningRepoId = viewModel.cloningRepoId.collectAsState().value
    val showCustomNameDialog = viewModel.showCustomNameDialog.collectAsState().value

    // State for the custom name input
    var customName by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            // List of repositories
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(repos) { repo ->
                    ListItem(
                        leadingContent = {
                            if (cloningRepoId.contains(repo.id)) {
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
                            // Show Build icon if repository is cloned locally
                            if (repo.isCloned) {
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = "Repository Cloned Locally",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                            }

                            // Show Add button if repository is not cloned
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

        // Show custom name dialog if needed
        showCustomNameDialog?.let { repo ->
            // Reset custom name when dialog is shown
            LaunchedEffect(repo) {
                customName = repo.name
            }

            AlertDialog(
                onDismissRequest = { viewModel.dismissCustomNameDialog() },
                title = { Text("Clone Repository") },
                text = {
                    Column {
                        Text("Enter a name for the cloned repository:")
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Repository Name") },
                            singleLine = true,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.cloneWithCustomName(customName.takeIf { it.isNotBlank() }) }
                    ) {
                        Text("Clone")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.dismissCustomNameDialog() }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
