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

import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
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
    Column(modifier = Modifier.padding(8.dp)) {
        // Search field for work items
        var searchText by remember { mutableStateOf("") }
        TextField(
            value = searchText,
            onValueChange = { searchText = it },
            label = { Text("Search Work Items") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )

        // Sample work items (placeholder)
        LazyColumn {
            // Header
            item {
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
            }

            // Sample work items
            items(sampleWorkItems) { workItem ->
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
                        workItem.assignedTo,
                        modifier = Modifier.weight(0.2f)
                    )
                }
                Divider()
            }
        }
    }
}

// Sample work item data class
data class WorkItem(
    val id: Int,
    val title: String,
    val state: String,
    val assignedTo: String
)

// Sample work items for UI demonstration
val sampleWorkItems = listOf(
    WorkItem(1, "Implement login functionality", "Active", "John Doe"),
    WorkItem(2, "Fix navigation bug", "Resolved", "Jane Smith"),
    WorkItem(3, "Add unit tests for API client", "New", "Unassigned"),
    WorkItem(4, "Update documentation", "Active", "John Doe"),
    WorkItem(5, "Refactor database access layer", "New", "Jane Smith")
)
