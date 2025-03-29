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
import net.tactware.nimbus.gitrepos.bl.RepositoryDownloadTracker
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
    private val repositoryDownloadTracker: RepositoryDownloadTracker,
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

    // Repository for which to show the custom name dialog
    private val _showCustomNameDialog = MutableStateFlow<GitRepo?>(null)
    val showCustomNameDialog = _showCustomNameDialog.asStateFlow()

    // Selected directory for cloning
    private val _selectedDirectory = MutableStateFlow<String?>(null)
    val selectedDirectory = _selectedDirectory.asStateFlow()



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
     * Starts the cloning process for the specified repository.
     * First shows a directory picker, then shows a dialog for entering a custom name.
     * 
     * @param repo The repository to clone
     */
    fun cloneRepository(repo: GitRepo) {
        // Start tracking this repository as being downloaded
        repositoryDownloadTracker.startDownloading(repo.id)

        viewModelScope.launch {
            // Show directory picker
            val directory = directoryPicker.pickDirectory("Select directory to clone ${repo.name}")

            if (directory != null) {
                // Store the selected directory
                _selectedDirectory.value = directory
                // Show the custom name dialog
                _showCustomNameDialog.value = repo
            } else {
                // If directory selection was canceled, stop tracking this repository
                repositoryDownloadTracker.stopDownloading(repo.id)
            }
        }
    }

    /**
     * Dismisses the custom name dialog without cloning.
     * Also stops tracking the repository as being downloaded.
     */
    fun dismissCustomNameDialog() {
        // Get the repository before clearing the dialog state
        val repo = _showCustomNameDialog.value

        // Clear the dialog state
        _showCustomNameDialog.value = null
        _selectedDirectory.value = null

        // Stop tracking the repository as being downloaded if it exists
        repo?.let { 
            repositoryDownloadTracker.stopDownloading(it.id)
        }
    }

    /**
     * Clones the repository with the provided custom name.
     * 
     * @param customName The custom name for the cloned repository, or null to use the default name
     */
    fun cloneWithCustomName(customName: String?) {
        val repo = _showCustomNameDialog.value
        val directory = _selectedDirectory.value

        if (repo != null && directory != null) {
            viewModelScope.launch {
                try {
                    // Clone repository with the custom name
                    cloneRepositoryUseCase(repo, directory, projectIdentifier, customName)

                    // Note: After rebuilding, the GitRepo objects will have isCloned and clonePath fields
                    // that will be automatically updated when the repository is refreshed
                } catch (e: Exception) {
                    // Handle any unexpected exceptions
                    println("Error cloning repository: ${e.message}")
                } finally {
                    // Clear the dialog state
                    _showCustomNameDialog.value = null
                    _selectedDirectory.value = null
                }
            }
        }
    }


}
