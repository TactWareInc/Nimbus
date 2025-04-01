package net.tactware.nimbus.projects.bl

import net.tactware.nimbus.appwide.bl.AzureDevOpsClient
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.koin.core.annotation.Factory

/**
 * Use case for updating the state of a work item in Azure DevOps.
 * This use case handles the creation of the AzureDevOpsClient with the appropriate project.
 */
@Factory
class UpdateWorkItemStateUseCase(
    private val getProjectByIdUseCase: GetProjectByIdUseCase
) {
    /**
     * Updates the state of a work item.
     *
     * @param projectIdentifier The identifier of the project the work item belongs to
     * @param workItemId The ID of the work item to update
     * @param newState The new state for the work item
     * @return True if the update was successful, false otherwise
     */
    suspend operator fun invoke(projectIdentifier: ProjectIdentifier, workItemId: Int, newState: String): Boolean {
        // Get the project
        val project = getProjectByIdUseCase(projectIdentifier.id) ?: return false

        // Create the Azure DevOps client with the project
        val azureDevOpsClient = AzureDevOpsClient(project)

        // Update the work item state
        return azureDevOpsClient.updateWorkItem(workItemId, newState)
    }
}
