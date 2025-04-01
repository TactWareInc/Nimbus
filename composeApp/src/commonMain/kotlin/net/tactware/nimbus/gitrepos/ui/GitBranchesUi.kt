package net.tactware.nimbus.gitrepos.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.gitrepos.dal.BranchWithRepo
import net.tactware.nimbus.gitrepos.dal.GitBranch
import net.tactware.nimbus.gitrepos.dal.GitRepo
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main UI for git branch management.
 */
@Composable
fun GitBranchesUi() {
    val viewModel = koinViewModel<GitBranchesViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val selectedRepo by viewModel.selectedRepo.collectAsState()
    val branches by viewModel.branches.collectAsState()

    // Fetch branches for all projects in the database
    // This allows viewing branches across all projects, not just a single one
    LaunchedEffect(Unit) {
        viewModel.fetchBranchesForAllProjects()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.medium)
    ) {
        Text(
            "Git Branch Management (All Projects)",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
        )

        when (uiState) {
            is GitBranchesViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is GitBranchesViewModel.UiState.Success -> {
                val repos = (uiState as GitBranchesViewModel.UiState.Success).repos

                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Repository list
                    Card(
                        modifier = Modifier.width(250.dp).fillMaxSize().padding(end = MaterialTheme.spacing.medium)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.small)
                        ) {
                            Text(
                                "Repositories",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                            )

                            Divider()

                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(top = MaterialTheme.spacing.small),
                                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                            ) {
                                items(repos) { repo ->
                                    RepoItem(
                                        repo = repo,
                                        isSelected = repo.id == selectedRepo?.id,
                                        onClick = { viewModel.selectRepository(repo) }
                                    )
                                }
                            }
                        }
                    }

                    // Branch list
                    Card(
                        modifier = Modifier.weight(1f).fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.small)
                        ) {
                            Text(
                                "Branches for ${selectedRepo?.name ?: "Selected Repository"}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                            )

                            Divider()

                            if (branches.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No branches found for this repository")
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(top = MaterialTheme.spacing.small),
                                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                                ) {
                                    items(branches) { branch ->
                                        BranchItem(
                                            branch = branch,
                                            onClick = { viewModel.switchBranch(branch.name) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is GitBranchesViewModel.UiState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No repositories found")
                }
            }
            is GitBranchesViewModel.UiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text((uiState as GitBranchesViewModel.UiState.Error).message)
                }
            }
        }
    }
}

/**
 * Repository item in the repository list.
 */
@Composable
fun RepoItem(
    repo: GitRepo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                repo.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Branch item in the branch list.
 */
@Composable
fun BranchItem(
    branch: GitBranch,
    onClick: () -> Unit,
    onDeleteLocally: () -> Unit = {}
) {
    // State for tracking if this branch item is expanded
    var expanded by remember { mutableStateOf(false) }

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
            // Main row with branch name and indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Branch type icon (remote or local)
                Icon(
                    imageVector = if (branch.isRemote) Icons.Default.Search else Icons.Default.Home,
                    contentDescription = if (branch.isRemote) "Remote Branch" else "Local Branch",
                    tint = if (branch.isRemote) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Branch name
                Text(
                    branch.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Current branch indicator
                if (branch.isCurrent) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Current Branch",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Expanded content with action buttons
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

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                    ) {
                        // Checkout button
                        Button(
                            onClick = { 
                                onClick()
                                expanded = false  // Collapse after action
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Checkout Branch",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    "Checkout",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }

                        // Delete locally button (only for non-current branches)
                        if (!branch.isCurrent) {
                            Button(
                                onClick = { 
                                    onDeleteLocally()
                                    expanded = false  // Collapse after action
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Branch Locally",
                                        tint = MaterialTheme.colorScheme.onError
                                    )
                                    Text(
                                        "Delete",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Branch with repository item in the branch list.
 */
@Composable
fun BranchWithRepoItem(
    branchWithRepo: BranchWithRepo,
    onClick: () -> Unit
) {
    // State for tracking if this branch item is expanded
    var expanded by remember { mutableStateOf(false) }

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
            // Main row with branch name and indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Branch type icon (remote or local)
                Icon(
                    imageVector = if (branchWithRepo.branch.isRemote) Icons.Default.Search else Icons.Default.Home,
                    contentDescription = if (branchWithRepo.branch.isRemote) "Remote Branch" else "Local Branch",
                    tint = if (branchWithRepo.branch.isRemote) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        branchWithRepo.branch.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        "Repository: ${branchWithRepo.repo.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                if (branchWithRepo.branch.isCurrent) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Current Branch",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Expanded content with action buttons
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

                    // Checkout button
                    Button(
                        onClick = { 
                            onClick()
                            expanded = false  // Collapse after action
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Checkout Branch",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Text(
                                "Checkout",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
