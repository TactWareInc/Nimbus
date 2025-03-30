package net.tactware.nimbus.projects.ui.specific

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * UI component for creating and managing work items.
 * This page allows users to:
 * 1. Create a bug work item
 * 2. Create or associate a branch with a work item
 */
@Composable
fun WorkItemPage(
    projectIdentifier: ProjectIdentifier,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<WorkItemPageViewModel> { parametersOf(projectIdentifier) }

    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val selectedRepos by viewModel.selectedRepos.collectAsState()
    val availableRepos by viewModel.availableRepos.collectAsState()
    val branchName by viewModel.branchName.collectAsState()
    val isCreatingWorkItem by viewModel.isCreatingWorkItem.collectAsState()
    val isCreatingBranch by viewModel.isCreatingBranch.collectAsState()
    val workItemCreated by viewModel.workItemCreated.collectAsState()
    val workItemId by viewModel.workItemId.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Work Item") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(MaterialTheme.spacing.medium)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            // Work Item Creation Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(MaterialTheme.spacing.medium)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                    ) {
                        Icon(
                            Icons.Default.Build,
                            contentDescription = "Bug",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Create Bug Work Item",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { viewModel.updateTitle(it) },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !workItemCreated
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        enabled = !workItemCreated
                    )

                    if (!workItemCreated) {
                        Button(
                            onClick = { viewModel.createWorkItem() },
                            modifier = Modifier.align(Alignment.End),
                            enabled = title.isNotBlank() && !isCreatingWorkItem
                        ) {
                            if (isCreatingWorkItem) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Create Bug")
                        }
                    } else {
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(MaterialTheme.spacing.small),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Build,
                                    contentDescription = "Bug Created",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Bug #$workItemId created successfully",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            // Branch Creation Section
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(MaterialTheme.spacing.medium)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Branch",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Create Branch",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    OutlinedTextField(
                        value = branchName,
                        onValueChange = { viewModel.updateBranchName(it) },
                        label = { Text("Branch Name") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = workItemCreated && !isCreatingBranch
                    )

                    Text(
                        "Select Repositories:",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (availableRepos.isEmpty()) {
                        Text(
                            "No repositories available. Please clone repositories first.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        availableRepos.forEach { repo ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Checkbox(
                                    checked = selectedRepos.contains(repo),
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            viewModel.selectRepo(repo)
                                        } else {
                                            viewModel.unselectRepo(repo)
                                        }
                                    },
                                    enabled = workItemCreated && !isCreatingBranch
                                )
                                Text(
                                    repo.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.createBranch() },
                        modifier = Modifier.align(Alignment.End),
                        enabled = workItemCreated && branchName.isNotBlank() && selectedRepos.isNotEmpty() && !isCreatingBranch
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
                }
            }
        }
    }
}
