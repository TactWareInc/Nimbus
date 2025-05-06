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
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import net.tactware.nimbus.appwide.bl.Package
import net.tactware.nimbus.appwide.bl.PackageVersion
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * UI component for displaying Artifactory packages for a specific project.
 */
@Composable
fun ArtifactoryUi(projectIdentifier: ProjectIdentifier) {
    val viewModel = koinViewModel<ArtifactoryViewModel> { parametersOf(projectIdentifier) }

    // Collect states from view model
    val packages = viewModel.packages.collectAsState().value
    val searchText = viewModel.searchText.collectAsState().value
    val isSearchMode = viewModel.isSearchMode.collectAsState().value
    val filteredPackages = viewModel.filteredPackages.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    val feedId = viewModel.feedId.collectAsState().value
    val isDownloading = viewModel.isDownloading.collectAsState().value
    val downloadingPackageId = viewModel.downloadingPackageId.collectAsState().value
    val showDownloadDialog = viewModel.showDownloadDialog.collectAsState().value
    val packageVersions = viewModel.packageVersions.collectAsState().value
    val selectedVersion = viewModel.selectedVersion.collectAsState().value
    val downloadPath = viewModel.downloadPath.collectAsState().value

    // State for the feed ID input
    var feedIdInput by remember { mutableStateOf(feedId) }
    var showFeedIdDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            // Feed ID and search row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Feed ID display with edit button
                Surface(
                    modifier = Modifier
                        .weight(0.4f)
                        .clickable { showFeedIdDialog = true },
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Feed: $feedId",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

                // Search field for packages
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { 
                        viewModel.updateSearchQuery(it)
                    },
                    label = { Text("Search Packages") },
                    leadingIcon = { 
                        Icon(
                            Icons.Filled.Search, 
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.primary
                        ) 
                    },
                    modifier = Modifier.weight(0.6f),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Loading indicator
            if (isLoading) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.small),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                        Text(
                            "Loading packages...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Packages header
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
                        "Type",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(0.3f)
                    )
                    Text(
                        "Actions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(0.2f)
                    )
                }
            }

            // Display packages based on search mode
            val displayPackages = if (isSearchMode) filteredPackages else packages

            if (displayPackages.isEmpty() && !isLoading) {
                // Show message for empty packages
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = MaterialTheme.spacing.small),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (isSearchMode) "No matching packages found" else "No packages found",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(MaterialTheme.spacing.medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // List of packages
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                items(displayPackages) { pkg ->
                    PackageRow(pkg, viewModel, downloadingPackageId)
                }
            }
        }

        // Feed ID dialog
        if (showFeedIdDialog) {
            AlertDialog(
                onDismissRequest = { showFeedIdDialog = false },
                title = { Text("Set Feed ID") },
                text = {
                    Column {
                        Text("Enter the Feed ID for Artifactory:")
                        OutlinedTextField(
                            value = feedIdInput,
                            onValueChange = { feedIdInput = it },
                            label = { Text("Feed ID") },
                            singleLine = true,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            viewModel.updateFeedId(feedIdInput)
                            showFeedIdDialog = false
                        }
                    ) {
                        Text("Set")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showFeedIdDialog = false }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Download dialog
        showDownloadDialog?.let { pkg ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissDownloadDialog() },
                title = { Text("Download Package") },
                text = {
                    Column {
                        Text("Select a version to download:")
                        
                        // Version dropdown
                        Box {
                            var expanded by remember { mutableStateOf(false) }
                            
                            OutlinedTextField(
                                value = selectedVersion?.version ?: "Select version",
                                onValueChange = { },
                                label = { Text("Version") },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clickable { expanded = true }
                            )
                            
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                packageVersions.forEach { version ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                "${version.version}${if (version.isLatest) " (Latest)" else ""}"
                                            ) 
                                        },
                                        onClick = {
                                            viewModel.selectVersion(version)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Download path
                        OutlinedTextField(
                            value = downloadPath,
                            onValueChange = { viewModel.updateDownloadPath(it) },
                            label = { Text("Download Path") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                        
                        if (isDownloading) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Downloading...")
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.downloadPackage() },
                        enabled = selectedVersion != null && downloadPath.isNotBlank() && !isDownloading
                    ) {
                        Text("Download")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { viewModel.dismissDownloadDialog() }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/**
 * Displays a single package row as a card.
 * When clicked, it expands to show more details and actions.
 */
@Composable
private fun PackageRow(
    pkg: Package,
    viewModel: ArtifactoryViewModel,
    downloadingPackageId: String?
) {
    var expanded by remember { mutableStateOf(false) }

    // Determine if this package is currently being downloaded
    val isDownloading = downloadingPackageId == pkg.id

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
                    pkg.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.5f)
                )

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                // Protocol type with subtle styling
                Text(
                    pkg.protocolType,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.3f)
                )

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                // Download button
                Button(
                    onClick = { viewModel.showDownloadDialog(pkg) },
                    enabled = !isDownloading,
                    modifier = Modifier.weight(0.2f)
                ) {
                    if (isDownloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            Icons.Filled.Download,
                            contentDescription = "Download Package",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Expanded content with details
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

                    // Package ID
                    Text(
                        "Package ID",
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
                            pkg.id,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(MaterialTheme.spacing.medium)
                        )
                    }

                    Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

                    // Package URL
                    Text(
                        "Package URL",
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
                            pkg.url,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(MaterialTheme.spacing.medium)
                        )
                    }
                }
            }
        }
    }
}