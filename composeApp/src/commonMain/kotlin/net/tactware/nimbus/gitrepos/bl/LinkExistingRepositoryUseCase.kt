package net.tactware.nimbus.gitrepos.bl

import net.tactware.nimbus.appwide.NotificationService
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.gitrepos.dal.GitReposRepository
import org.koin.core.annotation.Factory
import java.io.File
import org.eclipse.jgit.api.Git

/**
 * Use case for linking an existing local Git repository to a repository in the database.
 * This allows users to associate repositories that are already on their local machine
 * without having to clone them again.
 */
@Factory
class LinkExistingRepositoryUseCase(
    private val gitReposRepository: GitReposRepository
) {

    /**
     * Links an existing local Git repository to a repository in the database.
     * Verifies that the directory is a valid Git repository before linking.
     * 
     * @param repo The Git repository to link
     * @param localPath The path to the existing local repository
     * @return Result indicating success or failure with an error message
     */
    suspend operator fun invoke(repo: GitRepo, localPath: String): Result<Unit> {
        try {
            // Verify that the directory exists
            val repoDir = File(localPath)
            if (!repoDir.exists() || !repoDir.isDirectory) {
                return Result.failure(IllegalArgumentException("The specified path does not exist or is not a directory"))
            }

            // Verify that the directory is a Git repository
            try {
                // Try to open the repository to verify it's a valid Git repository
                Git.open(repoDir).use { git ->
                    // Get the remote URL to verify it matches the repo URL
                    val remotes = git.remoteList().call()

                    // Check if there's at least one remote
                    if (remotes.isEmpty()) {
                        return Result.failure(IllegalArgumentException("The specified directory is not a Git repository with remotes"))
                    }

                    // We don't strictly enforce URL matching as the remote URL might be in a different format
                    // (e.g., HTTPS vs SSH) or might be from a fork
                }
            } catch (e: Exception) {
                return Result.failure(IllegalArgumentException("The specified directory is not a valid Git repository: ${e.message}"))
            }

            // Update the repository in the database
            gitReposRepository.updateCloneStatus(
                repoId = repo.id,
                isCloned = true,
                clonePath = repoDir.absolutePath
            )

            // Send a notification to the user
            NotificationService.addNotification(
                title = "Repository Linked",
                message = "The repository '${repo.name}' has been successfully linked to ${repoDir.absolutePath}"
            )

            return Result.success(Unit)
        } catch (e: Exception) {
            // Log the error
            println("Error linking repository: ${e.message}")

            // Send a notification to the user about the error
            NotificationService.addNotification(
                title = "Repository Link Failed",
                message = "Failed to link repository '${repo.name}': ${e.message ?: "Unknown error"}"
            )

            return Result.failure(e)
        }
    }
}
