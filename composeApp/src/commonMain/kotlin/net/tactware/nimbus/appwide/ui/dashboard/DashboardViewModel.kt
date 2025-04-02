package net.tactware.nimbus.appwide.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.NotificationService
import net.tactware.nimbus.appwide.ui.main.MainViewModel
import net.tactware.nimbus.gitrepos.bl.FetchBranchesUseCase
import net.tactware.nimbus.gitrepos.dal.BranchWithRepo
import net.tactware.nimbus.gitrepos.dal.GitBranch
import net.tactware.nimbus.gitrepos.dal.GitBranchesRepository
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.gitrepos.dal.GitReposRepository
import net.tactware.nimbus.projects.dal.WorkItemsRepository
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import net.tactware.nimbus.projects.dal.entities.WorkItem
import org.koin.core.annotation.Factory

/**
 * ViewModel for the dashboard screen.
 * Provides data for recent branches, work items, and bug creation.
 */
@Factory
class DashboardViewModel(
    private val gitReposRepository: GitReposRepository,
    private val workItemsRepository: WorkItemsRepository,
    private val fetchBranchesUseCase: FetchBranchesUseCase,
    private val gitBranchesRepository: GitBranchesRepository
) : ViewModel() {

    // Recent branches
    private val _recentBranches = MutableStateFlow<List<BranchWithRepo>>(emptyList())
    val recentBranches: StateFlow<List<BranchWithRepo>> = _recentBranches.asStateFlow()

    // Recent work items
    private val _recentWorkItems = MutableStateFlow<List<WorkItem>>(emptyList())
    val recentWorkItems: StateFlow<List<WorkItem>> = _recentWorkItems.asStateFlow()

    // Total repositories count
    private val _totalRepositoriesCount = MutableStateFlow(0)
    val totalRepositoriesCount: StateFlow<Int> = _totalRepositoriesCount.asStateFlow()

    // Total work items count
    private val _totalWorkItemsCount = MutableStateFlow(0)
    val totalWorkItemsCount: StateFlow<Int> = _totalWorkItemsCount.asStateFlow()

    // Project repository counts
    private val _projectRepositoryCounts = MutableStateFlow<Map<ProjectIdentifier, Int>>(emptyMap())
    val projectRepositoryCounts: StateFlow<Map<ProjectIdentifier, Int>> = _projectRepositoryCounts.asStateFlow()

    // Project work item counts
    private val _projectWorkItemCounts = MutableStateFlow<Map<ProjectIdentifier, Int>>(emptyMap())
    val projectWorkItemCounts: StateFlow<Map<ProjectIdentifier, Int>> = _projectWorkItemCounts.asStateFlow()

    // Loading states
    private val _isLoadingBranches = MutableStateFlow(false)
    val isLoadingBranches: StateFlow<Boolean> = _isLoadingBranches.asStateFlow()

    private val _isLoadingWorkItems = MutableStateFlow(false)
    val isLoadingWorkItems: StateFlow<Boolean> = _isLoadingWorkItems.asStateFlow()

    private val _isLoadingRepositories = MutableStateFlow(false)
    val isLoadingRepositories: StateFlow<Boolean> = _isLoadingRepositories.asStateFlow()

    private val _isLoadingCounts = MutableStateFlow(false)
    val isLoadingCounts: StateFlow<Boolean> = _isLoadingCounts.asStateFlow()

    // Bug creation state
    private val _bugTitle = MutableStateFlow("")
    val bugTitle: StateFlow<String> = _bugTitle.asStateFlow()

    private val _bugDescription = MutableStateFlow("")
    val bugDescription: StateFlow<String> = _bugDescription.asStateFlow()

    private val _isCreatingBug = MutableStateFlow(false)
    val isCreatingBug: StateFlow<Boolean> = _isCreatingBug.asStateFlow()

    init {
        loadRealData()
    }

    /**
     * Loads real data from repositories.
     */
    private fun loadRealData() {
        viewModelScope.launch {
            // Load recent branches from cloned repositories
            _isLoadingBranches.value = true
            try {
                // Fetch branches for all projects
                fetchBranchesUseCase.fetchBranchesForAllProjects()

                // Get all branches from the repository
                val branchesWithRepos = getBranchesWithRepos()
                _recentBranches.value = branchesWithRepos
            } catch (e: Exception) {
                println("Error loading branches: ${e.message}")
            } finally {
                _isLoadingBranches.value = false
            }

            // Load recent work items from the repository
            _isLoadingWorkItems.value = true
            try {
                // Get work items from the repository
                val workItems = workItemsRepository.getWorkItems().first()

                // Sort by most recent first (assuming higher IDs are more recent)
                val sortedWorkItems = workItems.sortedByDescending { it.id }

                // Take the 5 most recent work items
                _recentWorkItems.value = sortedWorkItems.take(5)
            } catch (e: Exception) {
                println("Error loading work items: ${e.message}")
            } finally {
                _isLoadingWorkItems.value = false
            }

            // Load repository and work item counts
            loadCounts()
        }
    }

    /**
     * Loads repository and work item counts.
     */
    private fun loadCounts() {
        viewModelScope.launch {
            _isLoadingCounts.value = true
            try {
                // Get all work items
                val workItems = workItemsRepository.getWorkItems().first()
                _totalWorkItemsCount.value = workItems.size

                // Get projects from MainViewModel's state
                val mainViewModel = org.koin.core.context.GlobalContext.get().get<MainViewModel>()
                val projects = when (val state = mainViewModel.uiState.value) {
                    is MainViewModel.UiState.LoadedProjects -> state.projects
                    else -> emptyList()
                }

                // Calculate repository counts per project
                val repoCounts = mutableMapOf<ProjectIdentifier, Int>()
                var totalRepos = 0

                for (project in projects) {
                    val repos = gitReposRepository.getReposByProjectIdList(project.id)
                    repoCounts[project] = repos.size
                    totalRepos += repos.size
                }

                _projectRepositoryCounts.value = repoCounts
                _totalRepositoriesCount.value = totalRepos

                // Calculate work item counts per project
                // Note: Since we don't have a direct way to get work items by project,
                // we're using a placeholder implementation for now
                val workItemCounts = projects.associateWith { 0 }.toMutableMap()
                _projectWorkItemCounts.value = workItemCounts

            } finally {
                _isLoadingCounts.value = false
            }
        }
    }

    /**
     * Gets branches with their repositories from the GitBranchesRepository and GitReposRepository.
     * 
     * @return A list of BranchWithRepo objects
     */
    private suspend fun getBranchesWithRepos(): List<BranchWithRepo> {
        val result = mutableListOf<BranchWithRepo>()

        try {
            // Get all branches from the repository
            val allBranches = gitBranchesRepository.getAllBranches().first()

            // For each repository and its branches
            for ((repoId, branches) in allBranches) {
                // Get the repository from GitReposRepository
                val repo = gitReposRepository.getRepoById(repoId) ?: continue

                // Create BranchWithRepo objects for each branch
                for (branch in branches) {
                    result.add(BranchWithRepo(branch, repo))
                }
            }
        } catch (e: Exception) {
            println("Error getting branches with repos: ${e.message}")
        }

        // Sort by repository name and branch name
        return result.sortedWith(compareBy({ it.repo.name }, { it.branch.name }))
    }


    /**
     * Simulates switching to a branch.
     *
     * @param branchWithRepo The branch to switch to
     * @return Always returns true in this mock implementation
     */
    fun switchBranch(branchWithRepo: BranchWithRepo): Boolean {
        // In a real implementation, this would call the SwitchBranchUseCase
        // For now, just show a notification
        NotificationService.addNotification(
            title = "Branch Switched",
            message = "Switched to branch: ${branchWithRepo.branch.name} in repository: ${branchWithRepo.repo.name}"
        )
        return true
    }

    /**
     * Updates the bug title.
     *
     * @param title The new bug title
     */
    fun updateBugTitle(title: String) {
        _bugTitle.value = title
    }

    /**
     * Updates the bug description.
     *
     * @param description The new bug description
     */
    fun updateBugDescription(description: String) {
        _bugDescription.value = description
    }

    /**
     * Creates a new bug work item.
     *
     * @return Always returns 1234 in this mock implementation
     */
    fun createBug(): Int? {
        if (_bugTitle.value.isBlank()) {
            return null
        }

        // Launch a coroutine to handle the async operation
        viewModelScope.launch {
            _isCreatingBug.value = true
            try {
                // In a real implementation, this would call the AzureDevOpsClient
                // For now, just show a notification and delay to simulate network call
                delay(1000) // Simulate network delay
                NotificationService.addNotification(
                    title = "Bug Created",
                    message = "Created bug: ${_bugTitle.value}"
                )
            } finally {
                _isCreatingBug.value = false
                // Clear the form after creation
                _bugTitle.value = ""
                _bugDescription.value = ""
            }
        }

        return 1234 // Mock bug ID
    }
}
