package net.tactware.nimbus.projects.bl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.tactware.nimbus.appwide.bl.AzureDevOpsClient
import net.tactware.nimbus.projects.dal.ProjectsRepository
import net.tactware.nimbus.projects.dal.entities.Project
import org.koin.core.annotation.Factory
import kotlin.time.Duration.Companion.days

/**
 * Class responsible for checking if any PATs are about to expire and alerting users.
 */
@Factory
class PatExpirationChecker(
    private val projectsRepository: ProjectsRepository
) {
    companion object {
        // Default warning period: 7 days before expiration
        private val DEFAULT_WARNING_PERIOD = 7.days.inWholeMilliseconds
    }

    /**
     * Checks if any PATs are about to expire within the specified warning period.
     * 
     * @param warningPeriodMs The warning period in milliseconds. Default is 7 days.
     * @return A flow of projects with PATs that are about to expire.
     */
    fun getExpiringPats(warningPeriodMs: Long = DEFAULT_WARNING_PERIOD): Flow<List<Project>> {
        return projectsRepository.getAllProjectsFlow().map { projects ->
            val now = System.currentTimeMillis()
            projects.filter { project ->
                project.patExpirationDate?.let { expirationDate ->
                    expirationDate > now && expirationDate - now <= warningPeriodMs
                } ?: false
            }
        }
    }

    /**
     * Checks if a specific PAT is about to expire within the specified warning period.
     * 
     * @param project The project to check.
     * @param warningPeriodMs The warning period in milliseconds. Default is 7 days.
     * @return True if the PAT is about to expire, false otherwise.
     */
    fun isPatExpiring(project: Project, warningPeriodMs: Long = DEFAULT_WARNING_PERIOD): Boolean {
        val now = System.currentTimeMillis()
        return project.patExpirationDate?.let { expirationDate ->
            expirationDate > now && expirationDate - now <= warningPeriodMs
        } ?: false
    }

    /**
     * Checks if a specific PAT has already expired.
     * 
     * @param project The project to check.
     * @return True if the PAT has expired, false otherwise.
     */
    fun isPatExpired(project: Project): Boolean {
        val now = System.currentTimeMillis()
        return project.patExpirationDate?.let { expirationDate ->
            expirationDate <= now
        } ?: false
    }

    /**
     * Updates the PAT expiration date for a project.
     * 
     * @param projectName The name of the project.
     * @param expirationDate The new expiration date in milliseconds since epoch.
     */
    suspend fun updatePatExpirationDate(projectName: String, expirationDate: Long?) {
        projectsRepository.getProjectByName(projectName)?.let { project ->
            // Note: This will need to be updated once the ProjectsRepository is updated
            // to include the patExpirationDate parameter in the storeProject method.
            // For now, we'll just log a message.
            println("Updating PAT expiration date for project $projectName to $expirationDate")
        }
    }

    /**
     * Queries the Azure DevOps API to get the PAT expiration date.
     * 
     * @param project The project to query.
     * @return The PAT expiration date in milliseconds since epoch, or null if not available.
     */
    suspend fun queryPatExpirationDate(project: Project): Long? {
        try {
            // Create an AzureDevOpsClient for the project
            val client = AzureDevOpsClient(project)

            // Use the client to get the PAT expiration date
            return client.getPatExpirationDate()
        } catch (e: Exception) {
            println("Error querying PAT expiration date: ${e.message}")
            return null
        }
    }
}
