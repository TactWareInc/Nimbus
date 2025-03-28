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
import net.tactware.nimbus.projects.bl.GetProjectByIdUseCase
import net.tactware.nimbus.projects.bl.GetWorkItemsPagingDataUseCase
import net.tactware.nimbus.projects.bl.SearchWorkItemsUseCase
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import net.tactware.nimbus.projects.dal.entities.WorkItem
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam

/**
 * ViewModel for displaying work items with pagination and search support.
 */
@Factory
class WorkItemsViewModel(
    getWorkItemsPagingDataUseCase: GetWorkItemsPagingDataUseCase,
    private val searchWorkItemsUseCase: SearchWorkItemsUseCase
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
    internal val  workItemsPaging = getWorkItemsPagingDataUseCase()


    // Debounce job for search
    private var searchJob: Job? = null

    init {
        // Set up debounced search
        setupDebouncedSearch()
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
}
