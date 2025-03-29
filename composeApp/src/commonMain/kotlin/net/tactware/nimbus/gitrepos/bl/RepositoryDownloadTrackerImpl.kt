package net.tactware.nimbus.gitrepos.bl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.NotificationService
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.gitrepos.dal.GitReposRepository
import net.tactware.nimbus.projects.bl.GetProjectByIdUseCase
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.eclipse.jgit.api.Git
import org.koin.core.annotation.Single
import java.io.File

/**
 * Singleton implementation of RepositoryDownloadTracker interface.
 * Tracks repositories that are currently being downloaded/cloned and provides cloning functionality.
 */
@Single(binds = [RepositoryDownloadTracker::class])
class RepositoryDownloadTrackerImpl(
    private val gitReposRepository: GitReposRepository
) : RepositoryDownloadTracker {

    // Internal mutable state flow to track repository IDs that are being downloaded
    private val _downloadingRepoIds = MutableStateFlow<Set<Long>>(emptySet())

    // Public read-only state flow for observing downloading repository IDs
    override val downloadingRepoIds: StateFlow<Set<Long>> = _downloadingRepoIds.asStateFlow()

    /**
     * Starts tracking a repository as being downloaded
     *
     * @param repoId The ID of the repository being downloaded
     */
    override fun startDownloading(repoId: Long) {
        _downloadingRepoIds.update { currentIds ->
            currentIds + repoId
        }
    }

    /**
     * Stops tracking a repository as being downloaded (when download completes or fails)
     *
     * @param repoId The ID of the repository to stop tracking
     */
    override fun stopDownloading(repoId: Long) {
        _downloadingRepoIds.update { currentIds ->
            currentIds - repoId
        }
    }

    /**
     * Checks if a repository is currently being downloaded
     *
     * @param repoId The ID of the repository to check
     * @return True if the repository is being downloaded, false otherwise
     */
    override fun isDownloading(repoId: Long): Boolean {
        return _downloadingRepoIds.value.contains(repoId)
    }

    /**
     * Clears all downloading repositories (use with caution, mainly for testing)
     */
    override fun clearAll() {
        _downloadingRepoIds.value = emptySet()
    }

    /**
     * Clones a Git repository to the specified directory using JGit.
     * This method is not suspending as it launches its own coroutine to handle the cloning process.
     * It checks if the repository is already being downloaded and sends a notification if it is.
     *
     * @param repo The Git repository to clone
     * @param directory The directory to clone the repository to
     * @param projectIdentifier The identifier of the project that the repository belongs to
     * @param getProjectByIdUseCase Use case to get project details for authentication
     * @param customName Optional custom name for the cloned repository
     * @param gitReposRepository Repository for updating clone status
     * @return Result indicating success or failure with an error message
     */
    override fun cloneRepository(
        repo: GitRepo,
        directory: String,
        projectIdentifier: ProjectIdentifier?,
        getProjectByIdUseCase: GetProjectByIdUseCase,
        customName: String?,
    ): Result<Unit> {
        // Check if the repository is already being downloaded
        if (isDownloading(repo.id)) {
            // Send a notification to the user
            NotificationService.addNotification(
                title = "Repository Already Downloading",
                message = "The repository '${repo.name}' is already being downloaded."
            )
            return Result.success(Unit)
        }

        // Launch a coroutine to handle the cloning process
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Start tracking this repository download
                startDownloading(repo.id)

                // Create a File object for the target directory
                val targetDir = File(directory, customName ?: repo.name)

                // Ensure the parent directory exists
                targetDir.parentFile?.mkdirs()

                // Clone the repository using JGit
                val cloneCommand = Git.cloneRepository()
                    .setURI(repo.url)
                    .setDirectory(targetDir)

                // Add authentication if project identifier is provided
                if (projectIdentifier != null) {
                    // Get the project to access its PAT
                    val project = getProjectByIdUseCase(projectIdentifier.id)

                    // Add authentication if project is found and has a PAT
                    if (project != null && project.personalAccessToken.isNotBlank()) {
                        // For GitHub, GitLab, etc., PAT can be used as password with any username
                        cloneCommand.setCredentialsProvider(
                            org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider(
                                "oauth2",
                                project.personalAccessToken
                            )
                        )
                    }
                }

                cloneCommand.call().close()

                gitReposRepository.updateCloneStatus(
                    repoId = repo.id,
                    isCloned = true,
                    clonePath = targetDir.absolutePath
                )

                // Send a notification to the user that cloning is done
                NotificationService.addNotification(
                    title = "Repository Cloned",
                    message = "The repository '${repo.name}' has been successfully cloned to ${targetDir.absolutePath}"
                )
            } catch (e: Exception) {
                // Log the error
                println("Error cloning repository: ${e.message}")

                // Send a notification to the user about the error
                NotificationService.addNotification(
                    title = "Repository Clone Failed",
                    message = "Failed to clone repository '${repo.name}': ${e.message ?: "Unknown error"}"
                )
            } finally {
                // Stop tracking this repository download regardless of success or failure
                stopDownloading(repo.id)
            }
        }

        // Return success immediately since the cloning process is happening in a separate coroutine
        return Result.success(Unit)
    }
}
