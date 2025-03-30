package net.tactware.nimbus.projects.ui.specific

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.NotificationService
import net.tactware.nimbus.appwide.bl.AzureDevOpsClient
import net.tactware.nimbus.gitrepos.bl.GetReposByProjectIdUseCase
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.projects.bl.GetAllProjectNameUseCase
import net.tactware.nimbus.projects.bl.GetProjectByIdUseCase
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.eclipse.jgit.api.Git
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import java.io.File

/**
 * Enum class representing different types of work items.
 */
enum class WorkItemType(val displayName: String) {
    BUG("Bug"),
    TASK("Task"),
    USER_STORY("User Story"),
    FEATURE("Feature")
}

/**
 * ViewModel for the Work Item Page.
 * Handles creating work items and branches.
 */
@Factory
class WorkItemPageViewModel(
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val getReposByProjectIdUseCase: GetReposByProjectIdUseCase,
    private val getAllProjectNameUseCase: GetAllProjectNameUseCase
) : ViewModel() {

    // Work item fields
    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _workItemType = MutableStateFlow(WorkItemType.BUG)
    val workItemType: StateFlow<WorkItemType> = _workItemType.asStateFlow()

    // Project selection fields
    private val _selectedProject = MutableStateFlow<ProjectIdentifier?>(null)
    val selectedProject: StateFlow<ProjectIdentifier?> = _selectedProject.asStateFlow()

    private val _availableProjects = MutableStateFlow<List<ProjectIdentifier>>(emptyList())
    val availableProjects: StateFlow<List<ProjectIdentifier>> = _availableProjects.asStateFlow()

    // Branch fields
    private val _branchName = MutableStateFlow("")
    val branchName: StateFlow<String> = _branchName.asStateFlow()

    private val _selectedRepos = MutableStateFlow<List<GitRepo>>(emptyList())
    val selectedRepos: StateFlow<List<GitRepo>> = _selectedRepos.asStateFlow()

    private val _availableRepos = MutableStateFlow<List<GitRepo>>(emptyList())
    val availableRepos: StateFlow<List<GitRepo>> = _availableRepos.asStateFlow()

    // Status fields
    private val _isCreatingWorkItem = MutableStateFlow(false)
    val isCreatingWorkItem: StateFlow<Boolean> = _isCreatingWorkItem.asStateFlow()

    private val _isCreatingBranch = MutableStateFlow(false)
    val isCreatingBranch: StateFlow<Boolean> = _isCreatingBranch.asStateFlow()

    private val _workItemCreated = MutableStateFlow(false)
    val workItemCreated: StateFlow<Boolean> = _workItemCreated.asStateFlow()

    private val _workItemId = MutableStateFlow(0)
    val workItemId: StateFlow<Int> = _workItemId.asStateFlow()

    // Azure DevOps client
    private var azureDevOpsClient: AzureDevOpsClient? = null

    init {
        // Load available projects
        loadAvailableProjects()

        // Load project details and repositories if a project is selected
        if (_selectedProject.value != null) {
            loadProjectDetails()
            loadRepositories()
        }
    }

    /**
     * Loads available projects.
     */
    private fun loadAvailableProjects() {
        viewModelScope.launch {
            try {
                _availableProjects.value = getAllProjectNameUseCase()
            } catch (e: Exception) {
                NotificationService.addNotification(
                    title = "Error",
                    message = "Failed to load projects: ${e.message}"
                )
            }
        }
    }

    /**
     * Loads project details and initializes the Azure DevOps client.
     */
    private fun loadProjectDetails() {
        val selectedProject = _selectedProject.value ?: return

        viewModelScope.launch {
            val project = getProjectByIdUseCase(selectedProject.id)
            if (project != null) {
                azureDevOpsClient = AzureDevOpsClient(project)
            } else {
                NotificationService.addNotification(
                    title = "Error",
                    message = "Failed to load project details"
                )
            }
        }
    }

    /**
     * Loads repositories for the project.
     */
    private fun loadRepositories() {
        val selectedProject = _selectedProject.value ?: return

        viewModelScope.launch {
            getReposByProjectIdUseCase(selectedProject.id).collect { repos ->
                // Only show cloned repositories
                _availableRepos.value = repos.filter { it.isCloned && it.clonePath != null }
            }
        }
    }

    /**
     * Updates the selected work item type.
     */
    fun updateWorkItemType(type: WorkItemType) {
        _workItemType.value = type
    }

    /**
     * Updates the selected project.
     */
    fun updateSelectedProject(project: ProjectIdentifier?) {
        _selectedProject.value = project

        // Clear repositories when project changes
        _availableRepos.value = emptyList()
        _selectedRepos.value = emptyList()

        // Load project details and repositories if a project is selected
        if (project != null) {
            loadProjectDetails()
            loadRepositories()
        } else {
            azureDevOpsClient = null
        }
    }

    /**
     * Updates the work item title.
     */
    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    /**
     * Updates the work item description.
     */
    fun updateDescription(newDescription: String) {
        _description.value = newDescription
    }

    /**
     * Updates the branch name.
     */
    fun updateBranchName(newBranchName: String) {
        _branchName.value = newBranchName
    }

    /**
     * Selects a repository for branch creation.
     */
    fun selectRepo(repo: GitRepo) {
        _selectedRepos.value = _selectedRepos.value + repo
    }

    /**
     * Unselects a repository for branch creation.
     */
    fun unselectRepo(repo: GitRepo) {
        _selectedRepos.value = _selectedRepos.value - repo
    }

    /**
     * Creates a work item in Azure DevOps.
     * This is a placeholder implementation that simulates creating a work item.
     * In a real implementation, this would call the Azure DevOps API to create a work item.
     */
    fun createWorkItem() {
        if (_title.value.isBlank()) {
            NotificationService.addNotification(
                title = "Error",
                message = "Title cannot be empty"
            )
            return
        }

        _isCreatingWorkItem.value = true

        viewModelScope.launch {
            try {
                // Simulate API call delay
                kotlinx.coroutines.delay(1000)

                // In a real implementation, this would call the Azure DevOps API to create a work item
                // For now, we'll just simulate a successful creation with a random ID
                val newWorkItemId = (1000..9999).random()
                _workItemId.value = newWorkItemId
                _workItemCreated.value = true

                // Get the work item type name for the notification
                val workItemTypeName = _workItemType.value.displayName

                // Include project name in notification if a project is selected
                val projectInfo = _selectedProject.value?.let { " in project ${it.name}" } ?: ""

                NotificationService.addNotification(
                    title = "Success",
                    message = "$workItemTypeName #$newWorkItemId created successfully$projectInfo"
                )
            } catch (e: Exception) {
                NotificationService.addNotification(
                    title = "Error",
                    message = "Failed to create work item: ${e.message}"
                )
            } finally {
                _isCreatingWorkItem.value = false
            }
        }
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

        if (_selectedRepos.value.isEmpty()) {
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

                for (repo in _selectedRepos.value) {
                    try {
                        val repoPath = repo.clonePath
                        if (repoPath != null) {
                            val git = Git.open(File(repoPath))

                            // Create branch name with work item ID
                            val branchNameWithId = if (_workItemId.value > 0) {
                                "${_branchName.value}_${_workItemId.value}"
                            } else {
                                _branchName.value
                            }

                            // Create and checkout the branch
                            git.checkout()
                                .setCreateBranch(true)
                                .setName(branchNameWithId)
                                .call()

                            git.close()
                            successfulRepos.add(repo.name)
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
}
