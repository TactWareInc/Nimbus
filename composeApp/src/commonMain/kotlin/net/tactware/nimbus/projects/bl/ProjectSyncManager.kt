package net.tactware.nimbus.projects.bl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.bl.AzureDevOpsClient
import net.tactware.nimbus.projects.dal.ProjectsRepository
import net.tactware.nimbus.projects.dal.WorkItemsRepository
import net.tactware.nimbus.projects.dal.entities.Project
import org.koin.core.annotation.Single
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * A singleton manager for syncing projects with Azure DevOps.
 * Handles refreshing work items and other project-related data.
 */
@Single(createdAtStart = true)
class ProjectSyncManager(
    private val projectsRepository: ProjectsRepository,
    private val workItemsRepository: WorkItemsRepository,
    private val projectUpdater: ProjectUpdater
) {
    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        CoroutineScope(Dispatchers.Default).launch {
            // Initialize the sync process for all projects
            syncAllProjects()
            delay(60.toDuration(DurationUnit.SECONDS))
        }
    }

    /**
     * Syncs all known projects, refreshing their work items and other data.
     */
    fun syncAllProjects() {
        scope.launch {
            val projects = projectsRepository.getProjects()
            projects.forEach { project ->
                syncProject(project)
            }
        }
    }

    /**
     * Syncs a specific project by ID, refreshing its work items and other data.
     *
     * @param projectId The ID of the project to sync
     */
    suspend fun syncProjectById(projectId: String) {
        val project = projectsRepository.getProjects().find { it.id == projectId }
        if (project != null) {
            syncProject(project)
        } else {
            println("Project with ID $projectId not found")
        }
    }

    /**
     * Syncs a specific project, refreshing its work items and other data.
     *
     * @param project The project to sync
     */
    private suspend fun syncProject(project: Project) {
        // We can't directly call projectUpdater.update() because it requires a Uuid
        // Instead, we'll refresh the work items directly
        refreshWorkItems(project)
    }

    /**
     * Refreshes work items for a specific project.
     *
     * @param project The project whose work items should be refreshed
     */
    private suspend fun refreshWorkItems(project: Project) {
        val client = AzureDevOpsClient(project)

        // Fetch work items from Azure DevOps
        val workItems = client.getWorkItems()

        // Store each work item in the repository
        workItems.forEach { workItem ->
            workItemsRepository.storeWorkItem(workItem, project.id)
        }
    }
}
