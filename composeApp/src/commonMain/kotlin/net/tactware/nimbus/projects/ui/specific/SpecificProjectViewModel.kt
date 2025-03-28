package net.tactware.nimbus.projects.ui.specific

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.tactware.nimbus.gitrepos.bl.GetReposByProjectIdUseCase
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.projects.bl.GetProjectByIdUseCase
import net.tactware.nimbus.projects.bl.GetWorkItemsUseCase
import net.tactware.nimbus.projects.bl.ProjectUpdater
import net.tactware.nimbus.projects.dal.entities.WorkItem
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

@Factory
class SpecificProjectViewModel(
    @InjectedParam
    projectId: Uuid,
    projectUpdater: ProjectUpdater,
    getReposByProjectIdUseCase: GetReposByProjectIdUseCase,
    getProjectByIdUseCase: GetProjectByIdUseCase,
) : ViewModel() {

    private val _projectGitRepos = MutableStateFlow<List<GitRepo>>(emptyList())
    val projectGitRepos = _projectGitRepos.asStateFlow()

    init {
        viewModelScope.launch {
            projectUpdater.update(projectId)
        }

        viewModelScope.launch {
            getReposByProjectIdUseCase.invoke(projectId).collect {
                _projectGitRepos.value = it
            }
        }
    }
}
