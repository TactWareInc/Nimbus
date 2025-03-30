package net.tactware.nimbus.projects.ui.specific

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
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
    val isSearchMode = viewModel.isSearchMode.collectAsState().value
    val filteredRepos = viewModel.filteredRepos.collectAsState().value
    val isCloning = viewModel.isCloning.collectAsState().value
    val cloningRepoId = viewModel.cloningRepoId.collectAsState().value
    val showCustomNameDialog = viewModel.showCustomNameDialog.collectAsState().value

    // State for the custom name input
    var customName by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            // Search field for repositories with improved styling
            OutlinedTextField(
                value = searchText,
                onValueChange = { 
                    viewModel.updateSearchQuery(it)
                },
                label = { Text("Search Repositories") },
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

            // Loading indicator with improved styling
            if (isCloning) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.small),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Cloning repository...",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Repositories header with improved styling
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
                        "Name",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(0.5f)
                    )
                    Text(
                        "URL",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(0.3f)
                    )
                    Text(
                        "Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(0.2f)
                    )
                }
            }

            // Display repositories based on search mode
            val displayRepos = if (isSearchMode) filteredRepos else repos

            if (displayRepos.isEmpty()) {
                // Show message for empty repositories
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.small),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (isSearchMode) "No matching repositories found" else "No repositories found",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // List of repositories with improved styling
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                items(displayRepos) { repo ->
                    RepositoryRow(repo, viewModel, cloningRepoId)
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

/**
 * Displays a single repository row as a card.
 * When clicked, it expands to show more details and actions.
 */
@Composable
private fun RepositoryRow(
    repo: GitRepo,
    viewModel: RepositoriesViewModel,
    cloningRepoIds: List<Long>
) {
    var expanded by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Determine status color based on repository state
    val statusColor = when {
        cloningRepoIds.contains(repo.id) -> MaterialTheme.colorScheme.primary
        repo.isCloned -> MaterialTheme.colorScheme.tertiary
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
                // Name with emphasis
                Text(
                    repo.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.5f)
                )

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                // URL with subtle styling
                Text(
                    repo.url,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.3f)
                )

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                // Status with colored indicator
                Row(
                    modifier = Modifier.weight(0.2f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Colored circle indicator
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )

                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.tiny))

                    Text(
                        when {
                            cloningRepoIds.contains(repo.id) -> "Cloning"
                            repo.isCloned -> "Cloned"
                            else -> "Not Cloned"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Expanded content with details and actions
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

                    // Clone path if available
                    if (repo.isCloned && repo.clonePath != null) {
                        Text(
                            "Clone Path",
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
                                repo.clonePath,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(MaterialTheme.spacing.medium)
                            )
                        }

                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                    }

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                    ) {
                        // Link Existing Repository button
                        OutlinedButton(
                            onClick = { viewModel.linkExistingRepository(repo) },
                            enabled = !cloningRepoIds.contains(repo.id) && !repo.isCloned,
                            modifier = Modifier.padding(end = MaterialTheme.spacing.small)
                        ) {
                            Icon(
                                Icons.Filled.Build,
                                contentDescription = "Link Existing Repository",
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                            Text("Link Existing")
                        }

                        // Clone Repository button
                        Button(
                            onClick = { 
                                if (!repo.isCloned) {
                                    viewModel.cloneRepository(repo)
                                }
                            },
                            enabled = !cloningRepoIds.contains(repo.id) && !repo.isCloned
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Clone Repository",
                                modifier = Modifier.size(16.dp)
                            )

                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                            Text("Clone Repository")
                        }
                    }
                }
            }
        }
    }
}
