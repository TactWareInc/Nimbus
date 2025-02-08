package net.tactware.nimbus.projects.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import net.tactware.nimbus.projects.ui.addnew.NewProject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ShowProjects(projects: List<String>) {
    val projectsViewModel = koinViewModel<ProjectsViewModel> { parametersOf(projects) }
    Column(Modifier.fillMaxSize()) {
        ScrollableTabRow(projectsViewModel.selectedProject.collectAsState().value, Modifier.fillMaxWidth()) {
            projectsViewModel.projectsFlow.collectAsState().value.forEachIndexed { index, project ->
                Tab(selected = index == 0, onClick = {
                    projectsViewModel.onInteraction(ProjectsViewInteractions.SelectProject(project, index))
                }) {
                    Text(project)
                }
            }
            Tab(selected = false, onClick = { projectsViewModel.onInteraction(ProjectsViewInteractions.AddProject) }) {
                Text("Add Project")
            }
        }
        Box(Modifier.weight(1f)) {
            val state = projectsViewModel.uiState.collectAsState().value
            when (state) {
                is ProjectsViewModel.UiState.SpecificProject -> {
                    Text(state.project)
                }

                ProjectsViewModel.UiState.AddProject -> {
                    NewProject()
                }
            }
        }
    }
}