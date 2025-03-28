package net.tactware.nimbus.projects.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import net.tactware.nimbus.projects.ui.addnew.NewProject
import net.tactware.nimbus.projects.ui.specific.SpecificProjectUi
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ShowProjects(projects: List<ProjectIdentifier>, showAddProject: Boolean = false) {
    val projectsViewModel = koinViewModel<ProjectsViewModel> { parametersOf(projects) }

    // If showAddProject is true, show the Add Project screen directly
    if (showAddProject) {
        LaunchedEffect(Unit) {
            projectsViewModel.onInteraction(ProjectsViewInteractions.AddProject)
        }
    }

    // Collect state values outside of the LazyColumn
    val projectsList = projectsViewModel.projectsFlow.collectAsState().value
    val selectedProjectIndex = projectsViewModel.selectedProject.collectAsState().value
    val uiState = projectsViewModel.uiState.collectAsState().value

    // Use a Row layout for list pane detail scaffold
    Row(Modifier.fillMaxSize()) {
        // Left pane: Project list (30% width)
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.3f)
                .padding(MaterialTheme.spacing.small),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.small)
            ) {
                // Header
                Text(
                    "Projects",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(MaterialTheme.spacing.small)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                // Project list
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(projectsList) { project ->
                        // Find the index of the project
                        val index = projectsList.indexOf(project)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(MaterialTheme.spacing.small)
                                .clickable {
                                    projectsViewModel.onInteraction(ProjectsViewInteractions.SelectProject(project, index))
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedProjectIndex == index) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(MaterialTheme.spacing.medium)
                            ) {
                                Text(
                                    project.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }

                    // Add Project card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(MaterialTheme.spacing.small)
                                .clickable {
                                    projectsViewModel.onInteraction(ProjectsViewInteractions.AddProject)
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedProjectIndex == projectsList.size) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(MaterialTheme.spacing.medium),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Project",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                                Text(
                                    "Add Project",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right pane: Project details (70% width)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.7f)
                .padding(MaterialTheme.spacing.small)
        ) {
            when (uiState) {
                is ProjectsViewModel.UiState.SpecificProject -> {
                    SpecificProjectUi(uiState.project)
                }

                ProjectsViewModel.UiState.AddProject -> {
                    NewProject()
                }
            }
        }
    }
}
