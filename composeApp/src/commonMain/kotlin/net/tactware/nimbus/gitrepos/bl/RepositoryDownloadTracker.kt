package net.tactware.nimbus.gitrepos.bl

import kotlinx.coroutines.flow.StateFlow
import net.tactware.nimbus.appwide.NotificationService
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.projects.bl.GetProjectByIdUseCase
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Interface for tracking repositories that are currently being downloaded/cloned.
 * This allows the UI to show download status even if the user navigates away and comes back.
 */
interface RepositoryDownloadTracker {

    /**
     * Public read-only state flow for observing downloading repository IDs
     */
    val downloadingRepoIds: StateFlow<Set<Long>>

    /**
     * Starts tracking a repository as being downloaded
     * 
     * @param repoId The ID of the repository being downloaded
     */
    fun startDownloading(repoId: Long)

    /**
     * Stops tracking a repository as being downloaded (when download completes or fails)
     * 
     * @param repoId The ID of the repository to stop tracking
     */
    fun stopDownloading(repoId: Long)

    /**
     * Checks if a repository is currently being downloaded
     * 
     * @param repoId The ID of the repository to check
     * @return True if the repository is being downloaded, false otherwise
     */
    fun isDownloading(repoId: Long): Boolean

    /**
     * Clears all downloading repositories (use with caution, mainly for testing)
     */
    fun clearAll()

    /**
     * Clones a Git repository to the specified directory using JGit.
     * This method is not suspending as it launches its own coroutine to handle the cloning process.
     * It checks if the repository is already being downloaded and sends a notification if it is.
     * 
     * @param repo The Git repository to clone
     * @param directory The directory to clone the repository to
     * @param projectIdentifier The identifier of the project that the repository belongs to
     * @param getProjectByIdUseCase Use case to get project details for authentication
     * @param gitReposRepository Repository for updating clone status
     * @return Result indicating success or failure with an error message
     */
    fun cloneRepository(
        repo: GitRepo, 
        directory: String, 
        projectIdentifier: ProjectIdentifier? = null,
        getProjectByIdUseCase: GetProjectByIdUseCase,
    ): Result<Unit>
}
