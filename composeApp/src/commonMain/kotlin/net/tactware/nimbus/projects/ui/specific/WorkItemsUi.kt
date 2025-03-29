package net.tactware.nimbus.projects.ui.specific

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.ui.BrowserLauncher
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import net.tactware.nimbus.projects.dal.entities.WorkItem
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * UI component for displaying work items for a specific project.
 */
@Composable
fun WorkItemsUi(projectIdentifier: ProjectIdentifier) {
    val viewModel = koinViewModel<WorkItemsViewModel> { parametersOf(projectIdentifier) }
    val browserLauncher = koinInject<BrowserLauncher>()

    // Collect states from ViewModel
    val isSearchMode = viewModel.isSearchMode.collectAsState().value
    val searchResults = viewModel.searchResults.collectAsState().value
    val workItemsPaging = viewModel.workItemsPaging.collectAsLazyPagingItems()
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
                    WorkItemRow(workItem, viewModel, browserLauncher)
                    Divider()
                }
            }
        } else {
            // Display paged work items
            if (workItemsPaging.itemCount == 0 && !isLoading) {
                // Show loading indicator for initial paging data load
                Text(
                    "Loading work items...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(workItemsPaging.itemCount) { index ->
                    val workItem = workItemsPaging[index]
                    if (workItem != null) {
                        WorkItemRow(workItem, viewModel, browserLauncher)
                        Divider()
                    }
                }
            }
        }
    }
}

/**
 * Displays a single work item row.
 * When clicked, it expands to show the description and actions.
 */
@Composable
private fun WorkItemRow(
    workItem: WorkItem,
    viewModel: WorkItemsViewModel,
    browserLauncher: BrowserLauncher
) {
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 8.dp)
    ) {
        // Main row with basic information
        Row(
            modifier = Modifier.fillMaxWidth(),
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

        // Expanded content with description and actions
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 8.dp, end = 16.dp)
            ) {
                // Description
                if (workItem.description != null && workItem.description.isNotBlank()) {
                    Text(
                        "Description:",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        workItem.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                } else {
                    Text(
                        "No description available",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Open in browser button
                    IconButton(
                        onClick = { 
                            coroutineScope.launch {
                                val url = viewModel.getWorkItemUrl(workItem.id)
                                if (url != null) {
                                    println("Opening work item ${workItem.id} in browser: $url")
                                    browserLauncher.openUrl(url)
                                } else {
                                    println("Failed to get URL for work item ${workItem.id}")
                                }
                            }
                        }
                    ) {
                        Icon(
                            Icons.Filled.Search,
                            contentDescription = "Open in browser"
                        )
                    }
                    Text(
                        "Open in browser",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
