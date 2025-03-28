package net.tactware.nimbus.projects.bl

import net.tactware.nimbus.projects.dal.WorkItemsRepository
import net.tactware.nimbus.projects.dal.entities.WorkItem
import org.koin.core.annotation.Factory

/**
 * Use case for searching work items.
 * This use case returns a list of work items directly, not wrapped in a flow.
 */
@Factory
class SearchWorkItemsUseCase(private val workItemsRepository: WorkItemsRepository) {
    /**
     * Searches for work items matching the query.
     * 
     * @param query The search query
     * @return A list of work items matching the query
     */
    suspend operator fun invoke(query: String): List<WorkItem> {
        return if (query.isBlank()) {
            emptyList()
        } else {
            workItemsRepository.searchWorkItemsLikeFTS(query)
        }
    }
}