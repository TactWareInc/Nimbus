package net.tactware.nimbus.projects.ui.specific

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import net.tactware.nimbus.projects.ui.specific.WorkItemPage

/**
 * Main UI component for a specific project.
 * This component only handles tab selection and delegates to the appropriate UI components.
 */
@Composable
fun SpecificProjectUi(projectIdentifier: ProjectIdentifier) {
    // State for tab selection
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Repositories", "Work Items")

    // State for showing the work item page
    var showWorkItemPage by remember { mutableStateOf(false) }

    Scaffold { scaffoldPadding ->
        Column(modifier = Modifier.padding(scaffoldPadding)) {
            // Show either the main UI or the work item page
            if (showWorkItemPage) {
                WorkItemPage(
                    projectIdentifier = projectIdentifier,
                    onNavigateBack = { showWorkItemPage = false }
                )
            } else {
                // Tabs for repositories and work items
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) },
                            icon = {
                                if (index == 0) {
                                    Icon(Icons.Default.Build, contentDescription = "Repositories")
                                } else {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Work Items")
                                }
                            }
                        )
                    }
                }

                // Content based on selected tab
                when (selectedTabIndex) {
                    0 -> RepositoriesUi(projectIdentifier)
                    1 -> WorkItemsUi(
                        projectIdentifier = projectIdentifier,
                        onNavigateToCreateWorkItem = { showWorkItemPage = true }
                    )
                }
            }
        }
    }
}
