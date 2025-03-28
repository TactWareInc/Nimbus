package net.tactware.nimbus.projects.bl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import net.tactware.nimbus.projects.dal.WorkItemsRepository
import net.tactware.nimbus.projects.dal.entities.WorkItem
import org.koin.core.annotation.Factory

/**
 * Use case for retrieving work items, with optional search functionality.
 */
@Factory
class GetWorkItemsUseCase(private val workItemsRepository: WorkItemsRepository) {
    /**
     * Gets work items, optionally filtered by a search query.
     * 
     * @param searchQuery Optional search query to filter work items
     * @return A flow of work items matching the search query, or all work items if no query is provided
     */
    suspend operator fun invoke(searchQuery: String? = null): Flow<List<WorkItem>> {
        return if (searchQuery.isNullOrBlank()) {
            // No search query, return all work items
            workItemsRepository.getWorkItems()
        } else {
            // With search query, return search results as a flow
            flow {
                emit(workItemsRepository.searchWorkItems(searchQuery))
            }
        }
    }
}
