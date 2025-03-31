package net.tactware.nimbus.projects.ui.specific

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
// AzureDevOpsClient and BrowserLauncher are now handled by the ViewModel
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import net.tactware.nimbus.projects.dal.entities.WorkItem
// No longer using koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.alpha

/**
 * UI component for displaying work items in a List-Detail pattern.
 * This page allows users to:
 * 1. View a list of work items
 * 2. Select a work item to view its details
 * 3. Edit a work item's state
 */
@Composable
fun WorkItemListDetailPage(
    projectIdentifier: ProjectIdentifier,
    onNavigateBack: () -> Unit,
    onNavigateToCreateWorkItem: () -> Unit
) {
    val viewModel = koinViewModel<ProjectWorkItemsViewModel> { parametersOf(projectIdentifier) }
    val coroutineScope = rememberCoroutineScope()

    val isSearchMode by viewModel.isSearchMode.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val project by viewModel.project.collectAsState()

    // State for the selected work item
    var selectedWorkItem by remember { mutableStateOf<WorkItem?>(null) }

    // State for the edit state dialog
    var showEditStateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Work Items") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // List panel (left side)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(MaterialTheme.spacing.medium)
                        .fillMaxSize()
                ) {
                    // Search field with improved styling
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
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    // Work items list
                    if (isSearchMode) {
                        // Show search results
                        if (isLoading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            LazyColumn {
                                items(searchResults) { workItem ->
                                    WorkItemListItem(
                                        workItem = workItem,
                                        isSelected = selectedWorkItem?.id == workItem.id,
                                        onClick = { selectedWorkItem = workItem }
                                    )
                                }
                            }
                        }
                    } else {
                        // Show paged work items
                        val workItems = viewModel.workItemsPaging.collectAsLazyPagingItems()
                        LazyColumn {
                            items(workItems.itemCount) { index ->
                                workItems[index]?.let { workItem ->
                                    WorkItemListItem(
                                        workItem = workItem,
                                        isSelected = selectedWorkItem?.id == workItem.id,
                                        onClick = { selectedWorkItem = workItem }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    // Create new work item button
                    Button(
                        onClick = onNavigateToCreateWorkItem,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Create Work Item")
                    }
                }
            }

            // Divider between panels
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
            )

            // Detail panel (right side) with animation
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
            ) {
                // Use the new animated detail panel
                WorkItemDetailPanel(
                    workItem = selectedWorkItem,
                    onEditState = { showEditStateDialog = true },
                    onOpenInBrowser = { workItemId -> 
                        viewModel.openWorkItemInBrowser(workItemId)
                    }
                )
            }
        }
    }

    // Edit state dialog
    if (showEditStateDialog && selectedWorkItem != null) {
        EditStateDialog(
            workItem = selectedWorkItem!!,
            onDismiss = { showEditStateDialog = false },
            onStateChanged = { newState ->
                coroutineScope.launch {
                    val success = viewModel.updateWorkItemState(selectedWorkItem!!.id, newState)
                    if (success) {
                        // Update the selected work item with the new state
                        selectedWorkItem = selectedWorkItem!!.copy(state = newState)
                    }
                    showEditStateDialog = false
                }
            }
        )
    }
}

/**
 * Detail panel for displaying work item details with animation.
 */
@Composable
private fun WorkItemDetailPanel(
    workItem: WorkItem?,
    onEditState: () -> Unit,
    onOpenInBrowser: (Int) -> Unit
) {
    // Animate the alpha value based on whether a work item is selected
    val alpha by animateFloatAsState(
        targetValue = if (workItem != null) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "detailAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
    ) {
        if (workItem == null) {
            // No work item selected
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Select a work item to view details")
            }
        } else {
            // Work item details with animation
            Column(
                modifier = Modifier
                    .padding(MaterialTheme.spacing.medium)
                    .fillMaxSize()
            ) {
                // Header with title and edit button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "#${workItem.id} - ${workItem.title}",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    IconButton(onClick = onEditState) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit State")
                    }
                }

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                // Work item details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(MaterialTheme.spacing.medium)
                            .fillMaxWidth()
                    ) {
                        // Type and State
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Type: ${workItem.type}")
                            Text("State: ${workItem.state}")
                        }

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                        // Assigned To
                        Text("Assigned To: ${workItem.assignedTo}")

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                        // Description
                        Text(
                            text = "Description:",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(workItem.description ?: "")

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                        // Open in browser button
                        Button(
                            onClick = { onOpenInBrowser(workItem.id) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Open in Browser")
                        }
                    }
                }
            }
        }
    }
}

/**
 * A list item for displaying a work item in the list panel.
 */
@Composable
fun WorkItemListItem(
    workItem: WorkItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Animate elevation and shadow when selected
    val elevation by animateFloatAsState(
        targetValue = if (isSelected) 8f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "cardElevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(MaterialTheme.spacing.medium)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Work item ID with background
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "#${workItem.id}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            // Work item title
            Text(
                text = workItem.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            // Work item state with color based on state
            val stateColor = when (workItem.state.lowercase()) {
                "active", "in progress" -> MaterialTheme.colorScheme.primary
                "resolved", "closed", "done", "completed" -> MaterialTheme.colorScheme.tertiary
                "new" -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.outline
            }

            Surface(
                color = stateColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = workItem.state,
                    style = MaterialTheme.typography.bodySmall,
                    color = stateColor,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * Dialog for editing the state of a work item.
 */
@Composable
fun EditStateDialog(
    workItem: WorkItem,
    onDismiss: () -> Unit,
    onStateChanged: (String) -> Unit
) {
    // List of possible states (this should be dynamic based on the work item type)
    val possibleStates = listOf("New", "Active", "Resolved", "Closed")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change State") },
        text = {
            Column {
                Text("Current state: ${workItem.state}")
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                Text("Select new state:")

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                // State options
                possibleStates.forEach { state ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        color = if (state == workItem.state) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surface,
                        onClick = { if (state != workItem.state) onStateChanged(state) }
                    ) {
                        Text(
                            text = state,
                            modifier = Modifier.padding(MaterialTheme.spacing.small)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
