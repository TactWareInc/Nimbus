package net.tactware.nimbus.projects.dal

import app.cash.paging.Pager
import app.cash.paging.PagingConfig
import app.cash.paging.PagingData
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import migrations.net.tactware.nimbus.WorkItems
import net.tactware.nimbus.appwide.dal.IDatabaseProvider
import net.tactware.nimbus.db.NimbusDb
import net.tactware.nimbus.projects.dal.entities.WorkItem
import org.koin.core.annotation.Single

/**
 * Repository for managing WorkItems in the database.
 * 
 * IMPORTANT: This class depends on SQLDelight generated code from workitems.sq.
 * After adding workitems.sq and building the project, SQLDelight will generate:
 * 1. A WorkItems class representing the database entity
 * 2. A WorkitemsQueries class with methods for database operations
 * 3. The workitemsQueries property on the NimbusDb class
 *
 * Until then, this class will have compilation errors.
 */
@Single
class WorkItemsRepository(private val provider: IDatabaseProvider<NimbusDb>) {
     private val queries = provider.database.workitemsQueries

   /**
    * Creates a pager for work items with optional search functionality.
    * 
    * @param searchQuery Optional search query to filter work items
    * @return A Pager for work items
    */
   private fun createPager(searchQuery: String? = null): Pager<Int, WorkItem> = Pager(
       PagingConfig(
           pageSize = 50,
           initialLoadSize = 50,
       ),
       pagingSourceFactory = { 
           WorkItemPagingSource(
               loadPage = { limit, offset, query ->
                   if (query.isNullOrBlank()) {
                       // No search query, return all work items for the page
                       queries.getWorkItemsPage(limit, offset).executeAsList().map(mapper)
                   } else {
                       // With search query, filter work items
                       // Format the query for SQLite FTS using the helper method
                       val formattedQuery = formatSearchQuery(query)

                       // Note: This is a simple implementation that loads all items and filters in memory
                       // A more efficient implementation would use the search queries with LIMIT and OFFSET
                       queries.searchWorkItems(formattedQuery).executeAsList().map(mapper)
                   }
               },
               searchQuery = searchQuery
           )
       }
   )

    /**
     * Mapper from database entity to domain entity.
     * WorkItems is the SQLDelight generated class from workitems.sq.
     */
     private val mapper: (WorkItems) -> WorkItem = { dbWorkItem ->
         WorkItem(
             id = dbWorkItem.id.toInt(),
             title = dbWorkItem.title,
             description = dbWorkItem.description,
             state = dbWorkItem.state,
             assignedTo = dbWorkItem.assignedTo,
             type = dbWorkItem.type
         )
     }

    /**
     * Stores a work item in the database.
     * Checks if a work item with the same workItemId already exists in the given project.
     * If it exists, updates the existing row, otherwise creates a new one.
     */
    suspend fun storeWorkItem(workItem: WorkItem, project : String) {
         // Check if a work item with the same workItemId already exists in the given project
         val existingWorkItem = queries.getWorkItemByWorkItemIdAndProject(
             workItemId = workItem.id.toLong(),
             project = project
         ).executeAsOneOrNull()

         // If it exists, use its id, otherwise use the workItem's id
         val id = existingWorkItem?.id ?: workItem.id.toLong()

         queries.storeWorkItem(
             id = id,
             workItemId = workItem.id.toLong(),
             title = workItem.title,
             description = workItem.description,
             state = workItem.state,
             assignedTo = workItem.assignedTo,
             type = workItem.type,
             project = project
         )
    }

    /**
     * Gets all work items from the database.
     * This will be implemented after SQLDelight generates the necessary code.
     */
    suspend fun getWorkItems(): Flow<List<WorkItem>> {
         return queries.getAllWorkItems().asFlow().mapToList(Dispatchers.Default).map { it.map(mapper) }
    }

    /**
     * Gets a paging data flow of work items.
     * This uses the Paging library to load pages of work items on demand.
     * 
     * @param searchQuery Optional search query to filter work items
     * @return A flow of PagingData containing work items
     */
    fun getWorkItemsPagingData(searchQuery: String? = null): Flow<PagingData<WorkItem>> {
        return createPager(searchQuery).flow
    }

    /**
     * Gets a work item by ID.
     * This will be implemented after SQLDelight generates the necessary code.
     */
    suspend fun getWorkItem(id: Int): WorkItem? {
         return queries.getWorkItemById(id.toLong()).executeAsOneOrNull()?.let {
             mapper.invoke(it)
         }
    }

    /**
     * Deletes a work item by ID.
     * This will be implemented after SQLDelight generates the necessary code.
     */
    suspend fun deleteWorkItem(id: Int) {
         queries.deleteWorkItem(id.toLong())
    }

    // Search methods

    /**
     * Formats a search query for SQLite FTS using advanced syntax.
     * This method handles different types of searches:
     * 1. Work item ID search: If the query is a number, it's treated as a work item ID
     * 2. Title search: If the query starts with "title:", it's treated as a title search
     * 3. State search: If the query starts with "state:", it's treated as a state search
     * 4. General search: For all other queries, it uses advanced FTS syntax
     *
     * @param query The original search query
     * @return The formatted query for SQLite FTS
     */
    private fun formatSearchQuery(query: String): String {
        val trimmedQuery = query.trim()

        // Handle work item ID search
        if (isWorkItemIdSearch(trimmedQuery)) {
            // Search for the ID in the title field
            return "title:\"${trimmedQuery}\""
        }

        // Handle title search
        if (isTitleSearch(trimmedQuery)) {
            // Extract the title part and wrap in quotes for exact match
            val titleQuery = trimmedQuery.substringAfter("title:", "").trim()
            return "title:\"${titleQuery}\""
        }

        // Handle state search
        if (isStateSearch(trimmedQuery)) {
            // Extract the state part and wrap in quotes for exact match
            val stateQuery = trimmedQuery.substringAfter("state:", "").trim()
            return "state:\"${stateQuery}\""
        }

        // For general search, use advanced FTS syntax
        // Split the query into terms and handle each term
        return trimmedQuery.split(" ")
            .filter { it.isNotBlank() }
            .map { term ->
                // Check if the term is a phrase (contains multiple words in quotes)
                if (term.startsWith("\"") && term.endsWith("\"")) {
                    // Keep phrases as is
                    term
                } else {
                    // Add prefix matching for individual terms
                    "$term*"
                }
            }
            .joinToString(" OR ") // Use OR for more inclusive results
    }

    /**
     * Determines if the query is a direct work item ID search.
     * 
     * @param query The search query
     * @return True if the query is a number (work item ID), false otherwise
     */
    private fun isWorkItemIdSearch(query: String): Boolean {
        return query.trim().toIntOrNull() != null
    }

    /**
     * Determines if the query is a direct title search.
     * 
     * @param query The search query
     * @return True if the query starts with "title:", false otherwise
     */
    private fun isTitleSearch(query: String): Boolean {
        return query.trim().startsWith("title:", ignoreCase = true)
    }

    /**
     * Determines if the query is a direct state search.
     * 
     * @param query The search query
     * @return True if the query starts with "state:", false otherwise
     */
    private fun isStateSearch(query: String): Boolean {
        return query.trim().startsWith("state:", ignoreCase = true)
    }

    /**
     * Searches for work items matching the query across all fields.
     * The query is formatted for SQLite FTS with wildcards for prefix matching.
     */
    suspend fun searchWorkItems(query: String): List<WorkItem> {
         val formattedQuery = formatSearchQuery(query)
         return queries.searchWorkItems(formattedQuery).executeAsList().map(mapper)
    }

    /**
     * Searches for work items with titles matching the query.
     * The query is formatted for SQLite FTS with wildcards for prefix matching.
     */
    suspend fun searchWorkItemsByTitle(query: String): List<WorkItem> {
         val formattedQuery = formatSearchQuery(query)
         return queries.searchWorkItemsByTitle(formattedQuery).executeAsList().map(mapper)
    }

    /**
     * Searches for work items with states matching the query.
     * The query is formatted for SQLite FTS with wildcards for prefix matching.
     */
    suspend fun searchWorkItemsByState(query: String): List<WorkItem> {
         val formattedQuery = formatSearchQuery(query)
         return queries.searchWorkItemsByState(formattedQuery).executeAsList().map(mapper)
    }

    /**
     * Searches for work items with assignees matching the query.
     * The query is formatted for SQLite FTS with wildcards for prefix matching.
     */
    suspend fun searchWorkItemsByAssignee(query: String): List<WorkItem> {
         val formattedQuery = formatSearchQuery(query)
         return queries.searchWorkItemsByAssignee(formattedQuery).executeAsList().map(mapper)
    }

    /**
     * Searches for work items with types matching the query.
     * The query is formatted for SQLite FTS with wildcards for prefix matching.
     */
    suspend fun searchWorkItemsByType(query: String): List<WorkItem> {
         val formattedQuery = formatSearchQuery(query)
         return queries.searchWorkItemsByType(formattedQuery).executeAsList().map(mapper)
    }

    /***
     * Searches for work items using LIKE-based approach across all fields.
     * This method uses the LIKE operator instead of MATCH for more flexible pattern matching.
     * It searches across title, description, state, assignedTo, and type fields.
     * 
     * The SQL query for this method is:
     * 
     * searchWorkItemsLikeFTS:
     * WITH search_query AS (
     *     SELECT '%' || ? || '%' AS query
     * )
     * SELECT WorkItems.*
     * FROM WorkItemsFts
     * JOIN WorkItems ON WorkItems.id = WorkItemsFts.rowid
     * WHERE WorkItemsFts.title LIKE (SELECT query FROM search_query)
     *    OR WorkItemsFts.description LIKE (SELECT query FROM search_query)
     *    OR WorkItemsFts.state LIKE (SELECT query FROM search_query)
     *    OR WorkItemsFts.assignedTo LIKE (SELECT query FROM search_query)
     *    OR WorkItemsFts.type LIKE (SELECT query FROM search_query);
     * 
     * @param query The search query (will be wrapped with % wildcards)
     * @return A list of work items matching the query
     */
     suspend fun searchWorkItemsLikeFTS(query: String): List<WorkItem> {
         // No need to format the query, as the SQL query will add the % wildcards
         return queries.searchWorkItemsLikeFTS(query).executeAsList().map(mapper)
     }
}
