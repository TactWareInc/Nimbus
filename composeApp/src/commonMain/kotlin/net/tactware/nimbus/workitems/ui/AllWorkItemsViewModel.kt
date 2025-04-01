package net.tactware.nimbus.workitems.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import net.tactware.nimbus.projects.bl.SearchWorkItemsUseCase
import net.tactware.nimbus.projects.dal.WorkItemsRepository
import net.tactware.nimbus.projects.dal.entities.WorkItem
import org.koin.core.annotation.Factory

/**
 * ViewModel for displaying all work items across projects with filtering capabilities.
 */
@Factory
class AllWorkItemsViewModel(
    private val workItemsRepository: WorkItemsRepository,
    private val searchWorkItemsUseCase: SearchWorkItemsUseCase
) : ViewModel() {

    // All work items
    private val _workItems = MutableStateFlow<List<WorkItem>>(emptyList())
    val workItems = _workItems.asStateFlow()

    // Filtered work items
    private val _filteredWorkItems = MutableStateFlow<List<WorkItem>>(emptyList())
    val filteredWorkItems = _filteredWorkItems.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow<String>("")
    val searchQuery = _searchQuery.asStateFlow()

    // Active filters
    private val _activeFilters = MutableStateFlow<Set<WorkItemFilter>>(emptySet())
    val activeFilters = _activeFilters.asStateFlow()

    // Available states for filtering
    private val _availableStates = MutableStateFlow<List<String>>(emptyList())
    val availableStates = _availableStates.asStateFlow()

    // Available types for filtering
    private val _availableTypes = MutableStateFlow<List<String>>(emptyList())
    val availableTypes = _availableTypes.asStateFlow()

    // Debounce job for search
    private var searchJob: Job? = null

    init {
        // Load all work items
        loadWorkItems()

        // Set up debounced search
        setupDebouncedSearch()
    }

    /**
     * Loads all work items from the repository.
     */
    private fun loadWorkItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Collect work items from the repository
                workItemsRepository.getWorkItems().collect { items ->
                    _workItems.value = items
                    updateFilteredWorkItems()
                    
                    // Extract available states and types for filtering
                    _availableStates.value = items.mapNotNull { it.state }.distinct().sorted()
                    _availableTypes.value = items.mapNotNull { it.type }.distinct().sorted()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Sets up debounced search using the search query flow.
     */
    @OptIn(FlowPreview::class)
    private fun setupDebouncedSearch() {
        _searchQuery
            .debounce(300) // 300ms debounce to avoid rapid searches while typing
            .onEach { _ ->
                updateFilteredWorkItems()
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
     * Toggles a filter on or off.
     * 
     * @param filter The filter to toggle
     */
    fun toggleFilter(filter: WorkItemFilter) {
        val currentFilters = _activeFilters.value.toMutableSet()
        if (currentFilters.contains(filter)) {
            currentFilters.remove(filter)
        } else {
            currentFilters.add(filter)
        }
        _activeFilters.value = currentFilters
        updateFilteredWorkItems()
    }

    /**
     * Clears all active filters.
     */
    fun clearFilters() {
        _activeFilters.value = emptySet()
        updateFilteredWorkItems()
    }

    /**
     * Updates the filtered work items based on the search query and active filters.
     */
    private fun updateFilteredWorkItems() {
        val query = _searchQuery.value
        val filters = _activeFilters.value
        val allItems = _workItems.value

        // Apply search query and filters
        _filteredWorkItems.value = allItems.filter { workItem ->
            // Apply search query
            val matchesQuery = if (query.isBlank()) {
                true
            } else {
                workItem.title.contains(query, ignoreCase = true) ||
                workItem.description?.contains(query, ignoreCase = true) == true ||
                workItem.state.contains(query, ignoreCase = true) ||
                workItem.assignedTo?.contains(query, ignoreCase = true) == true ||
                workItem.type?.contains(query, ignoreCase = true) == true ||
                workItem.id.toString() == query
            }

            // Apply filters
            val matchesFilters = if (filters.isEmpty()) {
                true
            } else {
                filters.all { filter ->
                    when (filter) {
                        is WorkItemFilter.State -> workItem.state == filter.state
                        is WorkItemFilter.Type -> workItem.type == filter.type
                        is WorkItemFilter.Assigned -> workItem.assignedTo != null
                        is WorkItemFilter.Unassigned -> workItem.assignedTo == null
                    }
                }
            }

            matchesQuery && matchesFilters
        }
    }
}

/**
 * Sealed class representing different types of work item filters.
 */
sealed class WorkItemFilter {
    data class State(val state: String) : WorkItemFilter()
    data class Type(val type: String) : WorkItemFilter()
    object Assigned : WorkItemFilter()
    object Unassigned : WorkItemFilter()
}