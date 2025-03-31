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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

    // Fetch branches for the first project (hardcoded for now)
    // In a real implementation, you would get the project ID from the navigation or a parameter
    LaunchedEffect(Unit) {
        viewModel.fetchBranchesForProject("project1")
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.medium)
    ) {
        Text(
            "Git Branch Management",
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
                                modifier = Modifier.fillMaxSize().padding(top = MaterialTheme.spacing.small)
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
                                "All Branches",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                            )

                            Divider()

                            val allBranches by viewModel.allBranches.collectAsState()

                            if (allBranches.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No branches found")
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize().padding(top = MaterialTheme.spacing.small)
                                ) {
                                    items(allBranches) { branchWithRepo ->
                                        BranchWithRepoItem(
                                            branchWithRepo = branchWithRepo,
                                            onClick = { viewModel.switchBranchWithRepo(branchWithRepo) }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            repo.name,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Branch item in the branch list.
 */
@Composable
fun BranchItem(
    branch: GitBranch,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            branch.name,
            style = MaterialTheme.typography.bodyMedium
        )

        if (branch.isCurrent) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Current Branch",
                tint = MaterialTheme.colorScheme.primary
            )
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                branchWithRepo.branch.name,
                style = MaterialTheme.typography.bodyMedium
            )

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
}
