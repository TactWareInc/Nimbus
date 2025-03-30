package net.tactware.nimbus.workitems.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.tactware.nimbus.appwide.ui.BrowserLauncher
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.projects.dal.entities.WorkItem
import net.tactware.nimbus.projects.ui.specific.WorkItemPage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * UI component for displaying all work items across projects with filtering capabilities.
 */
@Composable
fun AllWorkItemsUi() {
    val viewModel = koinViewModel<AllWorkItemsViewModel>()
    val browserLauncher = koinInject<BrowserLauncher>()

    // State for showing the work item page
    var showWorkItemPage by remember { mutableStateOf(false) }

    // Collect states from ViewModel
    val workItems = viewModel.workItems.collectAsState().value
    val filteredWorkItems = viewModel.filteredWorkItems.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    val searchQuery = viewModel.searchQuery.collectAsState().value
    val activeFilters = viewModel.activeFilters.collectAsState().value
    val availableStates = viewModel.availableStates.collectAsState().value
    val availableTypes = viewModel.availableTypes.collectAsState().value

    // Show either the work item page or the main UI
    if (showWorkItemPage) {
        WorkItemPage(
            projectIdentifier = null,
            onNavigateBack = { showWorkItemPage = false }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.medium)) {
                // Header with title and create button
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.spacing.medium),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Work Items",
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Button(
                        onClick = { showWorkItemPage = true }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Create Work Item",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Create Work Item")
                    }
                }

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    label = { Text("Search Work Items") },
                    leadingIcon = { 
                        Icon(
                            Icons.Filled.Search, 
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(
                                    Icons.Filled.Clear,
                                    contentDescription = "Clear search",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = MaterialTheme.spacing.medium),
                    shape = RoundedCornerShape(8.dp)
                )

                // Filter chips for states
                if (availableStates.isNotEmpty()) {
                    Text(
                        "Filter by State",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = MaterialTheme.spacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        availableStates.take(5).forEach { state ->
                            val filter = WorkItemFilter.State(state)
                            val selected = activeFilters.contains(filter)

                            FilterChip(
                                selected = selected,
                                onClick = { viewModel.toggleFilter(filter) },
                                label = { Text(state) }
                            )
                        }
                    }
                }

                // Clear filters button
                if (activeFilters.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearFilters() }) {
                        Text("Clear Filters")
                    }
                }

                // Loading indicator
                if (isLoading) {
                    Text(
                        "Loading work items...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(MaterialTheme.spacing.medium)
                    )
                }

                // Work items list
                if (filteredWorkItems.isEmpty() && !isLoading) {
                    Text(
                        if (activeFilters.isEmpty() && searchQuery.isEmpty()) 
                            "No work items found" 
                        else 
                            "No work items match the current filters",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(MaterialTheme.spacing.medium)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        items(filteredWorkItems) { workItem ->
                            SimpleWorkItemRow(workItem)
                        }
                    }
                }

                // Status bar
                Text(
                    "Showing ${filteredWorkItems.size} of ${workItems.size} work items",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(MaterialTheme.spacing.small)
                )
            }

            // Floating action button for creating work items (alternative to the button in the header)
            FloatingActionButton(
                onClick = { showWorkItemPage = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(MaterialTheme.spacing.large)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Work Item"
                )
            }
        }
    }
}

/**
 * A simplified work item row that displays basic information.
 */
@Composable
private fun SimpleWorkItemRow(workItem: WorkItem) {
    // Determine state color based on work item state
    val stateColor = when (workItem.state.lowercase()) {
        "active", "in progress" -> MaterialTheme.colorScheme.primary
        "resolved", "closed", "done", "completed" -> MaterialTheme.colorScheme.tertiary
        "new" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Open work item details */ },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ID
            Text(
                workItem.id.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(0.1f)
            )

            // Title
            Text(
                workItem.title,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.5f)
            )

            // State with colored indicator
            Row(
                modifier = Modifier.weight(0.2f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(stateColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    workItem.state,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Assigned To
            Text(
                workItem.assignedTo ?: "Unassigned",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(0.2f)
            )
        }
    }
}
