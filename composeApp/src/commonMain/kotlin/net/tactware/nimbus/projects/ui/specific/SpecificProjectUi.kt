package net.tactware.nimbus.projects.ui.specific

import androidx.compose.runtime.Composable

import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SpecificProjectUi(projectIdentifier: ProjectIdentifier) {
    val viewModel = koinViewModel<SpecificProjectViewModel> { parametersOf(projectIdentifier.id) }



}