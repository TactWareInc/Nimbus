package net.tactware.nimbus.projects.ui.specific

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    
    Column {
        val repos = viewModel.projectGitRepos.collectAsState().value
        val searchText = viewModel.searchText.collectAsState().value

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
                    headlineContent = {
                        Text(repo.name)
                    },
                    supportingContent = {
                        Text(repo.url)
                    },
                    trailingContent = {
                        IconButton(onClick = {}) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }
                )
            }
        }
    }
}