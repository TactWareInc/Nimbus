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
import net.tactware.nimbus.projects.dal.customfields.CustomField
import net.tactware.nimbus.projects.dal.customfields.CustomFieldStore
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import net.tactware.nimbus.projects.ui.customfields.CustomFieldValue
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

    // Custom fields
    private val _customFieldValues = MutableStateFlow<List<CustomFieldValue>>(emptyList())
    val customFieldValues: StateFlow<List<CustomFieldValue>> = _customFieldValues.asStateFlow()

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
     * Sanitizes a string to create a valid Git branch name.
     * Removes spaces and special characters.
     */
    private fun sanitizeBranchName(input: String): String {
        // Replace spaces with hyphens and remove special characters
        return input.trim()
            .replace(" ", "-")
            .replace(Regex("[^a-zA-Z0-9-_.]"), "")
            .lowercase()
    }

    /**
     * Generates a branch name from the work item title.
     * Returns a sanitized version of the title that is valid for Git.
     */
    fun generateBranchNameFromTitle() {
        if (_title.value.isNotBlank()) {
            _branchName.value = sanitizeBranchName(_title.value)
        }
    }

    /**
     * Updates a custom field value.
     *
     * @param customFieldValue The custom field value to update
     */
    fun updateCustomFieldValue(customFieldValue: CustomFieldValue) {
        val currentValues = _customFieldValues.value
        val index = currentValues.indexOfFirst { it.field.name == customFieldValue.field.name }

        if (index != -1) {
            // Update existing value
            val updatedValues = currentValues.toMutableList()
            updatedValues[index] = customFieldValue
            _customFieldValues.value = updatedValues
        } else {
            // Add new value
            _customFieldValues.value = currentValues + customFieldValue
        }
    }

    /**
     * Checks if all required custom fields have values.
     *
     * @return True if all required custom fields have values, false otherwise
     */
    private fun areRequiredCustomFieldsValid(): Boolean {
        // Get custom fields for the selected project
        val projectId = _selectedProject.value?.id
        val customFields = CustomFieldStore.getCustomFieldsForProject(projectId)
        val requiredFields = customFields.filter { it.isRequired }

        if (requiredFields.isEmpty()) {
            return true
        }

        val currentValues = _customFieldValues.value
        return requiredFields.all { requiredField ->
            currentValues.any { it.field.name == requiredField.name && it.value.isNotBlank() }
        }
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
     * This method calls the Azure DevOps API to create a work item with the specified title, description, and type.
     * It also includes any custom fields that have been defined.
     */
    fun createWorkItem() {
        if (_title.value.isBlank()) {
            NotificationService.addNotification(
                title = "Error",
                message = "Title cannot be empty"
            )
            return
        }

        // Check if all required custom fields have values
        if (!areRequiredCustomFieldsValid()) {
            NotificationService.addNotification(
                title = "Error",
                message = "Please fill in all required custom fields"
            )
            return
        }

        // Check if Azure DevOps client is initialized
        if (azureDevOpsClient == null) {
            NotificationService.addNotification(
                title = "Error",
                message = "Azure DevOps client is not initialized. Please select a project first."
            )
            return
        }

        _isCreatingWorkItem.value = true

        viewModelScope.launch {
            try {
                // Call the Azure DevOps API to create a work item
                val workItemTypeString = _workItemType.value.displayName

                // Convert custom field values to a map
                val customFieldsMap = _customFieldValues.value.associate { 
                    it.field.name to it.value 
                }

                val newWorkItemId = azureDevOpsClient?.createWorkItem(
                    workItemType = workItemTypeString,
                    title = _title.value,
                    description = _description.value,
                    projectName = _selectedProject.value?.name,
                    customFields = customFieldsMap
                )

                if (newWorkItemId != null) {
                    // Work item created successfully
                    _workItemId.value = newWorkItemId
                    _workItemCreated.value = true

                    // Automatically generate a branch name from the title
                    generateBranchNameFromTitle()

                    // Get the work item type name for the notification
                    val workItemTypeName = _workItemType.value.displayName

                    // Include project name in notification if a project is selected
                    val projectInfo = _selectedProject.value?.let { " in project ${it.name}" } ?: ""

                    NotificationService.addNotification(
                        title = "Success",
                        message = "$workItemTypeName #$newWorkItemId created successfully$projectInfo"
                    )
                } else {
                    // Failed to create work item
                    NotificationService.addNotification(
                        title = "Error",
                        message = "Failed to create work item. Please check your connection and try again."
                    )
                }
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
