package net.tactware.nimbus.projects.ui.specific

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.ui.DirectoryPicker
import net.tactware.nimbus.gitrepos.bl.CloneRepositoryUseCase
import net.tactware.nimbus.gitrepos.bl.GetDownloadingReposUseCase
import net.tactware.nimbus.gitrepos.bl.GetReposByProjectIdUseCase
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

/**
 * ViewModel for displaying repositories for a specific project.
 */
@Factory
class RepositoriesViewModel(
    @InjectedParam
    private val projectIdentifier: ProjectIdentifier,
    private val getReposByProjectIdUseCase: GetReposByProjectIdUseCase,
    private val cloneRepositoryUseCase: CloneRepositoryUseCase,
    private val getDownloadingReposUseCase: GetDownloadingReposUseCase,
    private val directoryPicker: DirectoryPicker
) : ViewModel() {

    private val _projectGitRepos = MutableStateFlow<List<GitRepo>>(emptyList())
    val projectGitRepos = _projectGitRepos.asStateFlow()

    // Search query
    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    // Cloning state
    private val _isCloning = MutableStateFlow(false)
    val isCloning = _isCloning.asStateFlow()

    // Currently cloning repository ID
    private val _cloningRepoId = MutableStateFlow<List<Long>>(listOf())
    val cloningRepoId = _cloningRepoId.asStateFlow()



    init {
        viewModelScope.launch {
            getReposByProjectIdUseCase.invoke(projectIdentifier.id).collect {
                _projectGitRepos.value = it
            }
        }
        viewModelScope.launch(Dispatchers.Default) {
            getDownloadingReposUseCase.invoke().collect {
                // Update the cloning state based on the current downloading repositories
                _isCloning.value = it.isNotEmpty()
                _cloningRepoId.value = it.toList()
            }
        }
    }


    /**
     * Clones the specified repository.
     * 
     * @param repo The repository to clone
     */
    fun cloneRepository(repo: GitRepo) {
        viewModelScope.launch {
            // Show directory picker
            val directory = directoryPicker.pickDirectory("Select directory to clone ${repo.name}")

            if (directory != null) {
                try {
                    // Clone repository - the use case will update the RepositoryDownloadTracker
                    cloneRepositoryUseCase(repo, directory, projectIdentifier)

                    // Note: After rebuilding, the GitRepo objects will have isCloned and clonePath fields
                    // that will be automatically updated when the repository is refreshed
                } catch (e: Exception) {
                    // Handle any unexpected exceptions
                    println("Error cloning repository: ${e.message}")
                }
            }
        }
    }


}
