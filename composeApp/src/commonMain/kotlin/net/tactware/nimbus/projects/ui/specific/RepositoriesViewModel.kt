package net.tactware.nimbus.projects.ui.specific

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.ui.DirectoryPicker
import net.tactware.nimbus.gitrepos.bl.CloneRepositoryUseCase
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

    // Cloning message
    private val _cloningMessage = MutableStateFlow<String?>(null)
    val cloningMessage = _cloningMessage.asStateFlow()

    // Cloning result
    private val _cloningResult = MutableStateFlow<Result<Unit>?>(null)
    val cloningResult = _cloningResult.asStateFlow()

    init {
        viewModelScope.launch {
            getReposByProjectIdUseCase.invoke(projectIdentifier.id).collect {
                _projectGitRepos.value = it
            }
        }
    }

    /**
     * Updates the search query.
     * 
     * @param query The search query
     */
    fun updateSearchText(query: String) {
        _searchText.value = query
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
                // Set cloning state
                _isCloning.value = true
                _cloningMessage.value = "Cloning ${repo.name} to $directory..."
                _cloningResult.value = null

                try {
                    // Clone repository
                    val result = cloneRepositoryUseCase(repo, directory, projectIdentifier)
                    _cloningResult.value = result

                    // Update message based on result
                    if (result.isSuccess) {
                        _cloningMessage.value = "Successfully cloned ${repo.name} to $directory"
                    } else {
                        _cloningMessage.value = "Failed to clone ${repo.name}: ${result.exceptionOrNull()?.message}"
                    }
                } finally {
                    // Reset cloning state
                    _isCloning.value = false
                }
            }
        }
    }

    /**
     * Clears the cloning message.
     */
    fun clearCloningMessage() {
        _cloningMessage.value = null
    }

    /**
     * Clears the cloning result.
     */
    fun clearCloningResult() {
        _cloningResult.value = null
    }
}
