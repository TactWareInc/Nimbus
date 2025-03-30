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

    // Filtered repositories based on search query
    private val _filteredRepos = MutableStateFlow<List<GitRepo>>(emptyList())
    val filteredRepos = _filteredRepos.asStateFlow()

    // Search mode flag
    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode = _isSearchMode.asStateFlow()

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
                // Initialize filtered repos with all repos
                updateFilteredRepos(it, _searchText.value)
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
     * Updates the search query and filters repositories accordingly.
     * 
     * @param query The search query
     */
    fun updateSearchQuery(query: String) {
        _searchText.value = query
        _isSearchMode.value = query.isNotBlank()
        updateFilteredRepos(_projectGitRepos.value, query)
    }

    /**
     * Updates the filtered repositories based on the search query.
     * 
     * @param repos The list of repositories to filter
     * @param query The search query
     */
    private fun updateFilteredRepos(repos: List<GitRepo>, query: String) {
        if (query.isBlank()) {
            _filteredRepos.value = repos
            return
        }

        val lowerQuery = query.lowercase()
        _filteredRepos.value = repos.filter { repo ->
            repo.name.lowercase().contains(lowerQuery) ||
            repo.url.lowercase().contains(lowerQuery) ||
            repo.id.toString().contains(lowerQuery)
        }
    }


    /**
     * Starts the cloning process for the specified repository.
     * First shows a directory picker, then shows a dialog for entering a custom name.
     * 
     * @param repo The repository to clone
     */
    fun cloneRepository(repo: GitRepo) {
        viewModelScope.launch {
            // Show directory picker
            val directory = directoryPicker.pickDirectory("Select directory to clone ${repo.name}")

            if (directory != null) {
                // Store the selected directory
                _selectedDirectory.value = directory
                // Show the custom name dialog
                _showCustomNameDialog.value = repo
            }
        }
    }

    /**
     * Dismisses the custom name dialog without cloning.
     */
    fun dismissCustomNameDialog() {
        // Clear the dialog state
        _showCustomNameDialog.value = null
        _selectedDirectory.value = null
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
                    // Stop tracking this repository as being downloaded if there's an error
                    repositoryDownloadTracker.stopDownloading(repo.id)
                } finally {
                    // Clear the dialog state
                    _showCustomNameDialog.value = null
                    _selectedDirectory.value = null
                }
            }
        }
    }


}
