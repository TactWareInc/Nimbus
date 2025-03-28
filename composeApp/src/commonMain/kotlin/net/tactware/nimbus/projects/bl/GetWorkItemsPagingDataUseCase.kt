package net.tactware.nimbus.projects.bl

import app.cash.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.tactware.nimbus.projects.dal.WorkItemsRepository
import net.tactware.nimbus.projects.dal.entities.WorkItem
import org.koin.core.annotation.Factory

/**
 * Use case for retrieving work items with support for paging.
 * This uses the Paging library to load pages of work items on demand.
 */
@Factory
class GetWorkItemsPagingDataUseCase(private val workItemsRepository: WorkItemsRepository) {
    /**
     * Gets a paging data flow of work items.
     * 
     * @param searchQuery Optional search query to filter work items
     * @return A flow of PagingData containing work items
     */
    operator fun invoke(searchQuery: String? = null): Flow<PagingData<WorkItem>> {
        return workItemsRepository.getWorkItemsPagingData(searchQuery)
    }
}
