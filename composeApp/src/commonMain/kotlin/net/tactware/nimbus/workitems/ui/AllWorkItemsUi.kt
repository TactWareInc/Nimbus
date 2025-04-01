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
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.spring
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.ui.BrowserLauncher
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.appwide.utils.HtmlUtils
import net.tactware.nimbus.projects.dal.entities.WorkItem
import net.tactware.nimbus.projects.ui.specific.WorkItemPage
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * UI component for displaying all work items across projects with filtering capabilities.
 * Allows selecting a work item to view its details in a master-detail layout.
 */
@Composable
fun AllWorkItemsUi() {
    val viewModel = koinViewModel<AllWorkItemsViewModel>()
    val browserLauncher = koinInject<BrowserLauncher>()

    // State for showing the work item page
    var showWorkItemPage by remember { mutableStateOf(false) }

    // State for the selected work item
    var selectedWorkItem by remember { mutableStateOf<WorkItem?>(null) }

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
            Row(modifier = Modifier.fillMaxSize()) {
                // Left panel - Filter and list
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(MaterialTheme.spacing.medium)
                ) {
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
                                SimpleWorkItemRow(
                                    workItem = workItem,
                                    isSelected = selectedWorkItem?.id == workItem.id,
                                    onClick = { selectedWorkItem = workItem }
                                )
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

                // Divider between panels
                Divider(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                )

                // Right panel - Details
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxHeight()
                        .padding(MaterialTheme.spacing.medium)
                ) {
                    // Display work item details if a work item is selected
                    selectedWorkItem?.let { workItem ->
                        WorkItemDetailPanel(workItem = workItem)
                    } ?: run {
                        // Show a message when no work item is selected
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Select a work item to view details",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // Floating action button for creating work items (alternative to the button in the header)
            FloatingActionButton(
                onClick = { showWorkItemPage = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(MaterialTheme.spacing.large),
                containerColor = MaterialTheme.colorScheme.secondary
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
 * Can be expanded to show more details or selected to view in the details panel.
 */
@Composable
private fun SimpleWorkItemRow(
    workItem: WorkItem,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    // State for tracking if this work item is expanded
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val browserLauncher = koinInject<BrowserLauncher>()

    // Determine state color based on work item state
    val stateColor = when (workItem.state.lowercase()) {
        "active", "in progress" -> MaterialTheme.colorScheme.primary
        "resolved", "closed", "done", "completed" -> MaterialTheme.colorScheme.tertiary
        "new" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }

    // Background color based on selection state
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor)
                .padding(MaterialTheme.spacing.medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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

            // Expanded content with description and type
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(
                    animationSpec = spring(
                        dampingRatio = 0.7f,
                        stiffness = 300f
                    )
                ),
                exit = fadeOut(animationSpec = tween(300)) + shrinkVertically(
                    animationSpec = spring(
                        dampingRatio = 0.7f,
                        stiffness = 300f
                    )
                )
            ) {
                Column {
                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    // Type information if available
                    if (workItem.type != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Type:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = MaterialTheme.spacing.small)
                            )

                            Text(
                                workItem.type,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    }

                    // Description
                    if (workItem.description != null && workItem.description.isNotBlank()) {
                        Text(
                            "Description",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                        )

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                HtmlUtils.htmlToText(workItem.description),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(MaterialTheme.spacing.medium)
                            )
                        }
                    } else {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "No description available",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(MaterialTheme.spacing.medium)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Detail panel for displaying work item details.
 */
@Composable
private fun WorkItemDetailPanel(workItem: WorkItem) {
    val coroutineScope = rememberCoroutineScope()
    val browserLauncher = koinInject<BrowserLauncher>()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium)
    ) {
        // Header with title and ID
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "#${workItem.id} - ${workItem.title}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // State and type information
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            // State
            Column {
                Text(
                    "State",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    workItem.state,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }

            // Type if available
            if (workItem.type != null) {
                Column {
                    Text(
                        "Type",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        workItem.type,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Assigned to
        Column {
            Text(
                "Assigned To",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                workItem.assignedTo ?: "Unassigned",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Description
        Text(
            "Description",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (workItem.description != null && workItem.description.isNotBlank()) {
                Text(
                    HtmlUtils.htmlToText(workItem.description),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(MaterialTheme.spacing.medium)
                )
            } else {
                Text(
                    "No description available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.padding(MaterialTheme.spacing.medium)
                )
            }
        }

        // No "Open in Browser" button since WorkItem doesn't have a URL property
    }
}
