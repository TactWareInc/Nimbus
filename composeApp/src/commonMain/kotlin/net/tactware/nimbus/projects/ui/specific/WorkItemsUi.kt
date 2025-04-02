package net.tactware.nimbus.projects.ui.specific

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.spring
import app.cash.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.ui.BrowserLauncher
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.appwide.utils.HtmlUtils
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import net.tactware.nimbus.projects.dal.entities.WorkItem
import net.tactware.nimbus.gitrepos.ui.GitBranchesViewModel
import net.tactware.nimbus.gitrepos.dal.GitRepo
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * UI component for displaying work items for a specific project.
 * 
 * @param projectIdentifier The identifier of the project
 * @param onNavigateToCreateWorkItem Optional callback for navigating to the create work item page
 */
@Composable
fun WorkItemsUi(
    projectIdentifier: ProjectIdentifier,
    onNavigateToCreateWorkItem: (() -> Unit)? = null
) {
    val viewModel = koinViewModel<ProjectWorkItemsViewModel> { parametersOf(projectIdentifier) }
    val browserLauncher = koinInject<BrowserLauncher>()

    // Collect states from ViewModel
    val isSearchMode = viewModel.isSearchMode.collectAsState().value
    val searchResults = viewModel.searchResults.collectAsState().value
    val workItemsPaging = viewModel.workItemsPaging.collectAsLazyPagingItems()
    val isLoading = viewModel.isLoading.collectAsState().value

    Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
        // Search field for work items with improved styling
        val searchText = viewModel.searchQuery.collectAsState().value
        OutlinedTextField(
            value = searchText,
            onValueChange = { 
                viewModel.updateSearchQuery(it)
            },
            label = { Text("Search Work Items") },
            leadingIcon = { 
                Icon(
                    Icons.Filled.Search, 
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = MaterialTheme.spacing.medium),
            shape = RoundedCornerShape(8.dp)
        )

        // Create Work Item button (only shown if navigation callback is provided)
        if (onNavigateToCreateWorkItem != null) {
            Button(
                onClick = { onNavigateToCreateWorkItem() },
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(bottom = MaterialTheme.spacing.medium)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Work Item",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text("Create Work Item")
            }
        }

        // Loading indicator with improved styling
        if (isLoading) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.spacing.small),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Loading...",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(MaterialTheme.spacing.medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Work items header with improved styling
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = MaterialTheme.spacing.small),
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "ID",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(0.1f)
                )
                Text(
                    "Title",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(0.5f)
                )
                Text(
                    "State",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(0.2f)
                )
                Text(
                    "Assigned To",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(0.2f)
                )
            }
        }

        // Display work items based on mode (search or paging)
        if (isSearchMode) {
            // Display search results
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                items(searchResults) { workItem ->
                    WorkItemRow(workItem, viewModel, browserLauncher)
                }
            }
        } else {
            // Display paged work items
            if (workItemsPaging.itemCount == 0 && !isLoading) {
                // Show message for empty work items
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.small),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Loading work items...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                items(workItemsPaging.itemCount) { index ->
                    val workItem = workItemsPaging[index]
                    if (workItem != null) {
                        WorkItemRow(workItem, viewModel, browserLauncher)
                    }
                }
            }
        }
    }
}

/**
 * Displays a single work item row as a card.
 * When clicked, it expands to show the description and actions.
 */
@Composable
private fun WorkItemRow(
    workItem: WorkItem,
    viewModel: ProjectWorkItemsViewModel,
    browserLauncher: BrowserLauncher
) {
    var expanded by remember { mutableStateOf(false) }
    var showCreateBranchDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val gitBranchesViewModel = koinViewModel<GitBranchesViewModel>()

    // Determine state color based on work item state
    val stateColor = when (workItem.state.lowercase()) {
        "active", "in progress" -> MaterialTheme.colorScheme.primary
        "resolved", "closed", "done", "completed" -> MaterialTheme.colorScheme.tertiary
        "new" -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
        ) {
            // Main row with basic information
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // ID with subtle background
                Surface(
                    modifier = Modifier.weight(0.1f),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        workItem.id.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small, vertical = MaterialTheme.spacing.tiny),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                // Title with emphasis
                Text(
                    workItem.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.5f)
                )

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                // State with colored indicator
                Row(
                    modifier = Modifier.weight(0.2f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Colored circle indicator
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(stateColor)
                    )

                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.tiny))

                    Text(
                        workItem.state,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                // Assigned To with subtle styling
                Text(
                    workItem.assignedTo ?: "Unassigned",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (workItem.assignedTo == null) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) 
                    else 
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.2f)
                )
            }

            // Expanded content with description and actions
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

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Create Branch button
                        Button(
                            onClick = { showCreateBranchDialog = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.PlayArrow,
                                contentDescription = "Create Branch",
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                            Text("Create Branch")
                        }

                        // Open in browser button
                        Button(
                            onClick = { 
                                coroutineScope.launch {
                                    val url = viewModel.getWorkItemUrl(workItem.id)
                                    if (url != null) {
                                        println("Opening work item ${workItem.id} in browser: $url")
                                        browserLauncher.openUrl(url as String)
                                    } else {
                                        println("Failed to get URL for work item ${workItem.id}")
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Open in browser",
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                            Text("Open in browser")
                        }
                    }
                }
            }
        }
    }

    // Branch creation dialog
    if (showCreateBranchDialog) {
        // Function to sanitize branch name
        fun sanitizeBranchName(input: String): String {
            // Replace spaces with hyphens and remove special characters
            return input.trim()
                .replace(" ", "-")
                .replace(Regex("[^a-zA-Z0-9-_.]"), "")
                .lowercase()
        }

        // Generate branch name from work item title
        val initialBranchName = sanitizeBranchName(workItem.title) + "_" + workItem.id

        // Set initial branch name
        LaunchedEffect(showCreateBranchDialog) {
            gitBranchesViewModel.updateBranchName(initialBranchName)
        }

        // Collect states from ViewModel
        val branchName by gitBranchesViewModel.branchName.collectAsState()
        val selectedReposForBranch by gitBranchesViewModel.selectedReposForBranch.collectAsState()
        val isCreatingBranch by gitBranchesViewModel.isCreatingBranch.collectAsState()
        val pushToRemote by gitBranchesViewModel.pushToRemote.collectAsState()
        val autoCheckout by gitBranchesViewModel.autoCheckout.collectAsState()

        // Fetch repositories for the current project
        val project by viewModel.project.collectAsState()
        LaunchedEffect(showCreateBranchDialog) {
            project?.id?.let { projectId ->
                gitBranchesViewModel.fetchBranchesForProject(projectId)
            }
        }

        // Get repositories from UI state
        val uiState by gitBranchesViewModel.uiState.collectAsState()
        val repos = if (uiState is GitBranchesViewModel.UiState.Success) {
            (uiState as GitBranchesViewModel.UiState.Success).repos
        } else {
            emptyList()
        }

        AlertDialog(
            onDismissRequest = { 
                if (!isCreatingBranch) {
                    showCreateBranchDialog = false
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Create Branch",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Create Branch for Work Item #${workItem.id}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    OutlinedTextField(
                        value = branchName,
                        onValueChange = { gitBranchesViewModel.updateBranchName(it) },
                        label = { Text("Branch Name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isCreatingBranch
                    )

                    Text(
                        "Select Repositories:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (repos.isEmpty()) {
                        Text(
                            "No repositories available. Please clone repositories first.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        repos.forEach { repo ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = selectedReposForBranch.contains(repo),
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            gitBranchesViewModel.selectRepoForBranch(repo)
                                        } else {
                                            gitBranchesViewModel.unselectRepoForBranch(repo)
                                        }
                                    },
                                    enabled = !isCreatingBranch
                                )
                                Text(
                                    repo.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                    // Checkbox options
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = pushToRemote,
                            onCheckedChange = { gitBranchesViewModel.updatePushToRemote(it) },
                            enabled = !isCreatingBranch
                        )
                        Text(
                            "Let origin/remote know about the new branch",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = autoCheckout,
                            onCheckedChange = { gitBranchesViewModel.updateAutoCheckout(it) },
                            enabled = !isCreatingBranch
                        )
                        Text(
                            "Checkout the branch after creation",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        gitBranchesViewModel.createBranch()
                        showCreateBranchDialog = false
                    },
                    enabled = branchName.isNotBlank() && selectedReposForBranch.isNotEmpty() && !isCreatingBranch
                ) {
                    if (isCreatingBranch) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Create Branch")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCreateBranchDialog = false },
                    enabled = !isCreatingBranch
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
