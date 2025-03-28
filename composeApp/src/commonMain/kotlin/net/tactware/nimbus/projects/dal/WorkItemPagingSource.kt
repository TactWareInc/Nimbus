package net.tactware.nimbus.projects.dal

import app.cash.paging.PagingSource
import app.cash.paging.PagingState
import net.tactware.nimbus.projects.dal.entities.WorkItem

/**
 * WorkItemPagingSource is responsible for loading pages of work items from the database.
 * It implements the PagingSource interface from the Paging library.
 * 
 * @param loadPage Function to load a page of work items
 * @param searchQuery Optional search query to filter work items
 */
class WorkItemPagingSource(
    private val loadPage: suspend (limit: Long, offset: Long, searchQuery: String?) -> List<WorkItem>,
    private val searchQuery: String? = null
) : PagingSource<Int, WorkItem>() {

    override fun getRefreshKey(state: PagingState<Int, WorkItem>): Int? {
        // Try to find the page that was closest to the anchor position
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, WorkItem> {
        try {
            // Start page is 0 (first page)
            val page = params.key ?: 0
            val pageSize = params.loadSize

            // Calculate offset
            val offset = page * pageSize

            // Get work items for the current page
            val workItems = loadPage(pageSize.toLong(), offset.toLong(), searchQuery)

            // Calculate prev/next keys
            val prevKey = if (page > 0) page - 1 else null
            val nextKey = if (workItems.size >= pageSize) page + 1 else null

            return LoadResult.Page(
                data = workItems,
                prevKey = prevKey,
                nextKey = nextKey
            )
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }
}
