package net.tactware.nimbus.projects.ui.specific

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.ui.BrowserLauncher
import net.tactware.nimbus.projects.bl.GetProjectByIdUseCase
import net.tactware.nimbus.projects.bl.GetWorkItemsPagingDataUseCase
import net.tactware.nimbus.projects.bl.SearchWorkItemsUseCase
import net.tactware.nimbus.projects.bl.UpdateWorkItemStateUseCase
import net.tactware.nimbus.projects.dal.entities.Project
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import net.tactware.nimbus.projects.dal.entities.WorkItem
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

/**
 * ViewModel for displaying work items for a specific project with pagination and search support.
 */
@Factory
class ProjectWorkItemsViewModel(
    getWorkItemsPagingDataUseCase: GetWorkItemsPagingDataUseCase,
    private val searchWorkItemsUseCase: SearchWorkItemsUseCase,
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val browserLauncher: BrowserLauncher,
    private val updateWorkItemStateUseCase: UpdateWorkItemStateUseCase,
    @InjectedParam private val projectIdentifier: ProjectIdentifier
) : ViewModel() {


    // Work items for search results
    private val _searchResults = MutableStateFlow<List<WorkItem>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery = _searchQuery.asStateFlow()

    // Flag to indicate if we're in search mode
    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode = _isSearchMode.asStateFlow()

    // Work items paging data (used when not in search mode)
    internal val workItemsPaging = getWorkItemsPagingDataUseCase()

    // Project details
    private val _project = MutableStateFlow<Project?>(null)
    val project = _project.asStateFlow()

    // Selected work item ID
    private val _selectedWorkItemId = MutableStateFlow<Int?>(null)
    val selectedWorkItemId = _selectedWorkItemId.asStateFlow()

    // Debounce job for search
    private var searchJob: Job? = null

    init {
        // Set up debounced search
        setupDebouncedSearch()

        // Load project details
        loadProject()
    }

    /**
     * Loads the project details using the GetProjectByIdUseCase.
     */
    private fun loadProject() {
        viewModelScope.launch {
            val projectDetails = getProjectByIdUseCase(projectIdentifier.id)

            _project.value = projectDetails
        }
    }

    /**
     * Gets the URL for a work item.
     * 
     * @param workItemId The ID of the work item
     * @return The URL for the work item, or null if the project details are not available
     */
    suspend fun getWorkItemUrl(workItemId: Int): String? {
        val projectDetails = project.value ?: getProjectByIdUseCase(projectIdentifier.id)
        return projectDetails?.let {
            "${it.orgOrCollectionUrl}/${it.projectName}/_workitems/edit/$workItemId"
        }
    }

    /**
     * Sets up debounced search using the search query flow.
     */
    @OptIn(FlowPreview::class)
    private fun setupDebouncedSearch() {
        _searchQuery
            .debounce(300) // 300ms debounce to avoid rapid searches while typing
            .onEach { query ->
                if (query.isBlank()) {
                    // If query is blank, switch to paging mode
                    _isSearchMode.value = false
                } else {
                    // If query is not blank, switch to search mode and perform search
                    _isSearchMode.value = true
                    performSearch(query)
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Updates the search query, which will trigger the debounced search.
     * 
     * @param query The search query
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Performs a search using the GetWorkItemsUseCase.
     * 
     * @param query The search query
     */
    private fun performSearch(query: String) {
        // Cancel any existing search job
        searchJob?.cancel()

        // Start a new search job
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                // Get search results directly as a list
                val results = searchWorkItemsUseCase(query)
                _searchResults.value = results
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Opens a work item in the browser.
     * 
     * @param workItemId The ID of the work item to open
     */
    fun openWorkItemInBrowser(workItemId: Int) {
        viewModelScope.launch {
            val url = getWorkItemUrl(workItemId)
            if (url != null) {
                browserLauncher.openUrl(url)
            }
        }
    }

    /**
     * Updates the state of a work item.
     * 
     * @param workItemId The ID of the work item to update
     * @param newState The new state for the work item
     * @return True if the update was successful, false otherwise
     */
    suspend fun updateWorkItemState(workItemId: Int, newState: String): Boolean {
        return updateWorkItemStateUseCase(projectIdentifier, workItemId, newState)
    }

    /**
     * Updates the selected work item ID.
     * 
     * @param workItemId The ID of the selected work item, or null to clear the selection
     */
    fun updateSelectedWorkItemId(workItemId: Int?) {
        _selectedWorkItemId.value = workItemId
    }
}
