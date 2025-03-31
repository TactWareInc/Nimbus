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
import org.koin.core.annotation.Factory

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

    /**
     * Fetches branches for a project.
     *
     * @param projectId The ID of the project
     */
    fun fetchBranchesForProject(projectId: String) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
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
            switchBranchUseCase.switchBranch(repo.id, branchName)
        }
    }

    /**
     * Switches to a different branch in the repository that the branch belongs to.
     *
     * @param branchWithRepo The branch with repository information to switch to
     */
    fun switchBranchWithRepo(branchWithRepo: BranchWithRepo) {
        viewModelScope.launch {
            switchBranchUseCase.switchBranch(branchWithRepo.repo.id, branchWithRepo.branch.name)
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
