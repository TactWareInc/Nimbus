package net.tactware.nimbus.projects.ui.specific

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems

import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import net.tactware.nimbus.projects.dal.entities.WorkItem
import net.tactware.nimbus.projects.ui.ExposedSearchMenu
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SpecificProjectUi(projectIdentifier: ProjectIdentifier) {
    val viewModel = koinViewModel<SpecificProjectViewModel> { parametersOf(projectIdentifier.id) }

    // State for tab selection
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Repositories", "Work Items")

    Scaffold { scaffoldPadding ->
        Column(modifier = Modifier.padding(scaffoldPadding)) {
            // Project name header
            Text(
                text = projectIdentifier.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(8.dp)
            )

            // Tabs for repositories and work items
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) },
                        icon = {
                            if (index == 0) {
                                Icon(Icons.Default.Build, contentDescription = "Repositories")
                            } else {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Work Items")
                            }
                        }
                    )
                }
            }

            // Content based on selected tab
            when (selectedTabIndex) {
                0 -> RepositoriesTab(viewModel)
                1 -> WorkItemsTab(projectIdentifier)
            }
        }
    }
}

@Composable
fun RepositoriesTab(viewModel: SpecificProjectViewModel) {
    Column {
        val repos = viewModel.projectGitRepos.collectAsState().value

        // Search functionality
        var searchText by remember { mutableStateOf("") }
        val test = listOf("Test", "Test2", "Test3")
        var filteredList = derivedStateOf {
            test.filter { it.contains(searchText, ignoreCase = true) }
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
                    value = searchText,
                    onValueChange = {
                        searchText = it
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

@Composable
fun WorkItemsTab(projectIdentifier: ProjectIdentifier) {
    val viewModel = koinViewModel<WorkItemsViewModel> { parametersOf(projectIdentifier) }

    // Collect states from ViewModel
    val isSearchMode = viewModel.isSearchMode.collectAsState().value
    val searchResults = viewModel.searchResults.collectAsState().value
    val workItemsPaging = viewModel.workItemsPaging.collectAsState().value.collectAsLazyPagingItems()
    val isLoading = viewModel.isLoading.collectAsState().value

    Column(modifier = Modifier.padding(8.dp)) {
        // Search field for work items
        val searchText = viewModel.searchQuery.collectAsState().value
        TextField(
            value = searchText,
            onValueChange = { 
                viewModel.updateSearchQuery(it)
            },
            label = { Text("Search Work Items") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // Loading indicator
        if (isLoading) {
            Text(
                "Loading...",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }

        // Work items header
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "ID",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(0.1f)
            )
            Text(
                "Title",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(0.5f)
            )
            Text(
                "State",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(0.2f)
            )
            Text(
                "Assigned To",
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(0.2f)
            )
        }
        Divider()

        // Display work items based on mode (search or paging)
        if (isSearchMode) {
            // Display search results
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(searchResults) { workItem ->
                    WorkItemRow(workItem)
                    Divider()
                }
            }
        } else {
            // Display paged work items
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(workItemsPaging.itemCount) { index ->
                    val workItem = workItemsPaging[index]
                    if (workItem != null) {
                        WorkItemRow(workItem)
                        Divider()
                    }
                }
            }
        }
    }
}

/**
 * Displays a single work item row.
 */
@Composable
private fun WorkItemRow(workItem: WorkItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            workItem.id.toString(),
            modifier = Modifier.weight(0.1f)
        )
        Text(
            workItem.title,
            modifier = Modifier.weight(0.5f)
        )
        Text(
            workItem.state,
            modifier = Modifier.weight(0.2f)
        )
        Text(
            workItem.assignedTo ?: "Unassigned",
            modifier = Modifier.weight(0.2f)
        )
    }
}

/**
 * NOTE: This is a placeholder for how to implement the WorkItemsTab using the Cash App's Paging library.
 * To implement this properly, you would need to:
 * 1. Add the app.cash.paging:paging-compose-common dependency
 * 2. Update WorkItemsViewModel to expose a Flow<PagingData<WorkItem>>
 * 3. Implement a PagingSource in WorkItemsRepository
 * 
 * The Compose Paging library provides built-in support for pagination, including:
 * - Automatic loading of pages as the user scrolls
 * - Built-in loading and error states
 * - Efficient recycling of items
 * - Support for placeholders
 */
