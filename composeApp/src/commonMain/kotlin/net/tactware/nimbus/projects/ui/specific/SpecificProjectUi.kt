package net.tactware.nimbus.projects.ui.specific

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem

import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SpecificProjectUi(projectIdentifier: ProjectIdentifier) {
    val viewModel = koinViewModel<SpecificProjectViewModel> { parametersOf(projectIdentifier.id) }

    Scaffold { scaffoldPadding ->
        val repos = viewModel.projectGitRepos.collectAsState().value

        Text("Git Repos for ${projectIdentifier.name}")

        LazyColumn {
            items(repos) { repo ->
                ListItem(
                    headlineContent = {
                        Text(repo.name)
                    },
                    supportingContent = {
                        Text(repo.url)
                    },
                    trailingContent = {
                        IconButton(onClick = {

                        }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                )
            }
        }
    }
}