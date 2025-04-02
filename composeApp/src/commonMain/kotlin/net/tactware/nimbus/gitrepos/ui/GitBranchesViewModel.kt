package net.tactware.nimbus.gitrepos.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.tactware.nimbus.gitrepos.bl.FetchBranchesUseCase
import net.tactware.nimbus.gitrepos.bl.SwitchBranchUseCase
import net.tactware.nimbus.gitrepos.dal.BranchWithRepo
import net.tactware.nimbus.gitrepos.dal.GitBranch
import net.tactware.nimbus.gitrepos.dal.GitBranchesRepository
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.gitrepos.dal.GitReposRepository
import net.tactware.nimbus.appwide.NotificationService
import org.eclipse.jgit.api.Git
import org.koin.core.annotation.Factory
import java.io.File

/**
 * ViewModel for git branch management.
 */
@Factory
class GitBranchesViewModel(
    private val gitBranchesRepository: GitBranchesRepository,
    private val gitReposRepository: GitReposRepository,
    private val fetchBranchesUseCase: FetchBranchesUseCase,
    private val switchBranchUseCase: SwitchBranchUseCase
) : ViewModel() {

    // UI state
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Selected repository
    private val _selectedRepo = MutableStateFlow<GitRepo?>(null)
    val selectedRepo: StateFlow<GitRepo?> = _selectedRepo.asStateFlow()

    // Branches for the selected repository
    private val _branches = MutableStateFlow<List<GitBranch>>(emptyList())
    val branches: StateFlow<List<GitBranch>> = _branches.asStateFlow()

    // All branches with their repository information
    private val _allBranches = MutableStateFlow<List<BranchWithRepo>>(emptyList())
    val allBranches: StateFlow<List<BranchWithRepo>> = _allBranches.asStateFlow()

    // Current project ID
    private var currentProjectId: String = ""

    // Branch creation state
    private val _branchName = MutableStateFlow("")
    val branchName: StateFlow<String> = _branchName.asStateFlow()

    private val _selectedReposForBranch = MutableStateFlow<List<GitRepo>>(emptyList())
    val selectedReposForBranch: StateFlow<List<GitRepo>> = _selectedReposForBranch.asStateFlow()

    private val _isCreatingBranch = MutableStateFlow(false)
    val isCreatingBranch: StateFlow<Boolean> = _isCreatingBranch.asStateFlow()

    private val _pushToRemote = MutableStateFlow(true)
    val pushToRemote: StateFlow<Boolean> = _pushToRemote.asStateFlow()

    private val _autoCheckout = MutableStateFlow(true)
    val autoCheckout: StateFlow<Boolean> = _autoCheckout.asStateFlow()

    /**
     * Fetches branches for a project.
     *
     * @param projectId The ID of the project
     */
    fun fetchBranchesForProject(projectId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                // Store the current project ID
                currentProjectId = projectId

                val repos = fetchBranchesUseCase.fetchBranchesForProject(projectId)
                if (repos.isEmpty()) {
                    _uiState.value = UiState.Empty
                } else {
                    _uiState.value = UiState.Success(repos)
                    // Select the first repository by default
                    selectRepository(repos.first())

                    // Fetch branches from all repositories
                    fetchAllBranches(repos)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to fetch branches: ${e.message}")
            }
        }
    }

    /**
     * Fetches branches for all projects in the database.
     * This allows viewing branches across all projects, not just the current one.
     */
    fun fetchBranchesForAllProjects() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val repos = fetchBranchesUseCase.fetchBranchesForAllProjects()
                if (repos.isEmpty()) {
                    _uiState.value = UiState.Empty
                } else {
                    _uiState.value = UiState.Success(repos)
                    // Select the first repository by default
                    selectRepository(repos.first())

                    // Fetch branches from all repositories
                    fetchAllBranches(repos)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to fetch branches: ${e.message}")
            }
        }
    }

    /**
     * Fetches branches from all repositories and updates the allBranches state flow.
     *
     * @param repos The list of repositories to fetch branches from
     */
    private suspend fun fetchAllBranches(repos: List<GitRepo>) {
        val allBranchesWithRepo = mutableListOf<BranchWithRepo>()

        // For each repository, get its branches and add them to the list with repository information
        repos.forEach { repo ->
            gitBranchesRepository.getBranchesByRepoId(repo.id).collect { branches ->
                val branchesWithRepo = branches.map { branch ->
                    BranchWithRepo(branch, repo)
                }
                allBranchesWithRepo.addAll(branchesWithRepo)
                _allBranches.value = allBranchesWithRepo
            }
        }
    }

    /**
     * Selects a repository and fetches its branches.
     *
     * @param repo The repository to select
     */
    fun selectRepository(repo: GitRepo) {
        viewModelScope.launch {
            _selectedRepo.value = repo

            // Fetch branches for the repository if we have a current project ID
            if (currentProjectId.isNotEmpty()) {
                fetchBranchesUseCase.fetchBranchesForRepo(repo.id, currentProjectId)
            }

            // Observe branches for the selected repository
            gitBranchesRepository.getBranchesByRepoId(repo.id).collect { branches ->
                _branches.value = branches
            }
        }
    }

    /**
     * Switches to a different branch in the selected repository.
     *
     * @param branchName The name of the branch to switch to
     */
    fun switchBranch(branchName: String) {
        viewModelScope.launch {
            val repo = _selectedRepo.value ?: return@launch
            val success = switchBranchUseCase.switchBranch(repo.id, branchName)
            if (success) {
                NotificationService.addNotification(
                    title = "Branch Switched",
                    message = "Successfully switched to branch: $branchName in repository: ${repo.name}"
                )
            }
        }
    }

    /**
     * Switches to a different branch in the repository that the branch belongs to.
     *
     * @param branchWithRepo The branch with repository information to switch to
     */
    fun switchBranchWithRepo(branchWithRepo: BranchWithRepo) {
        viewModelScope.launch {
            val success = switchBranchUseCase.switchBranch(branchWithRepo.repo.id, branchWithRepo.branch.name)
            if (success) {
                NotificationService.addNotification(
                    title = "Branch Switched",
                    message = "Successfully switched to branch: ${branchWithRepo.branch.name} in repository: ${branchWithRepo.repo.name}"
                )
            }
        }
    }

    /**
     * Updates the branch name.
     *
     * @param name The new branch name
     */
    fun updateBranchName(name: String) {
        _branchName.value = name
    }

    /**
     * Selects a repository for branch creation.
     *
     * @param repo The repository to select
     */
    fun selectRepoForBranch(repo: GitRepo) {
        val currentList = _selectedReposForBranch.value.toMutableList()
        if (!currentList.contains(repo)) {
            currentList.add(repo)
            _selectedReposForBranch.value = currentList
        }
    }

    /**
     * Unselects a repository for branch creation.
     *
     * @param repo The repository to unselect
     */
    fun unselectRepoForBranch(repo: GitRepo) {
        val currentList = _selectedReposForBranch.value.toMutableList()
        currentList.remove(repo)
        _selectedReposForBranch.value = currentList
    }

    /**
     * Updates the push to remote option.
     *
     * @param value The new value
     */
    fun updatePushToRemote(value: Boolean) {
        _pushToRemote.value = value
    }

    /**
     * Updates the auto checkout option.
     *
     * @param value The new value
     */
    fun updateAutoCheckout(value: Boolean) {
        _autoCheckout.value = value
    }

    /**
     * Creates a branch in the selected repositories.
     * This creates a new branch in each selected repository based on the current HEAD.
     */
    fun createBranch() {
        if (_branchName.value.isBlank()) {
            NotificationService.addNotification(
                title = "Error",
                message = "Branch name cannot be empty"
            )
            return
        }

        if (_selectedReposForBranch.value.isEmpty()) {
            NotificationService.addNotification(
                title = "Error",
                message = "Please select at least one repository"
            )
            return
        }

        _isCreatingBranch.value = true

        viewModelScope.launch {
            try {
                val successfulRepos = mutableListOf<String>()
                val failedRepos = mutableListOf<Pair<String, String>>()

                for (repo in _selectedReposForBranch.value) {
                    try {
                        val repoPath = repo.clonePath
                        if (repoPath != null) {
                            val git = Git.open(File(repoPath))

                            if (_autoCheckout.value) {
                                // Create and checkout the branch
                                git.checkout()
                                    .setCreateBranch(true)
                                    .setName(_branchName.value)
                                    .call()
                            } else {
                                // Create the branch without checking it out
                                git.branchCreate()
                                    .setName(_branchName.value)
                                    .call()
                            }

                            // Push to remote if option is selected
                            if (_pushToRemote.value) {
                                try {
                                    git.push()
                                        .setRemote("origin")
                                        .add(_branchName.value)
                                        .call()
                                } catch (e: Exception) {
                                    // Log the error but continue with other operations
                                    println("Failed to push branch to remote: ${e.message}")
                                }
                            }

                            git.close()
                            successfulRepos.add(repo.name)

                            // Refresh branches for this repository
                            fetchBranchesUseCase.fetchBranchesForRepo(repo.id, currentProjectId)
                        } else {
                            failedRepos.add(Pair(repo.name, "Repository path is null"))
                        }
                    } catch (e: Exception) {
                        failedRepos.add(Pair(repo.name, e.message ?: "Unknown error"))
                    }
                }

                // Show notification with results
                if (successfulRepos.isNotEmpty()) {
                    NotificationService.addNotification(
                        title = "Branches Created",
                        message = "Successfully created branches in: ${successfulRepos.joinToString(", ")}"
                    )

                    // Clear the branch name and selected repos after successful creation
                    _branchName.value = ""
                    _selectedReposForBranch.value = emptyList()
                }

                if (failedRepos.isNotEmpty()) {
                    NotificationService.addNotification(
                        title = "Branch Creation Failed",
                        message = "Failed to create branches in: ${failedRepos.map { "${it.first} (${it.second})" }.joinToString(", ")}"
                    )
                }
            } catch (e: Exception) {
                NotificationService.addNotification(
                    title = "Error",
                    message = "Failed to create branches: ${e.message}"
                )
            } finally {
                _isCreatingBranch.value = false
            }
        }
    }

    /**
     * UI state for the git branches screen.
     */
    sealed class UiState {
        /**
         * Loading state.
         */
        object Loading : UiState()

        /**
         * Success state with a list of repositories.
         *
         * @property repos The list of repositories
         */
        data class Success(val repos: List<GitRepo>) : UiState()

        /**
         * Empty state when no repositories are found.
         */
        object Empty : UiState()

        /**
         * Error state when an error occurs.
         *
         * @property message The error message
         */
        data class Error(val message: String) : UiState()
    }
}
