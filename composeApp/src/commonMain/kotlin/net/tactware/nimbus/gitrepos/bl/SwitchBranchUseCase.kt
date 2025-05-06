package net.tactware.nimbus.gitrepos.bl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.tactware.nimbus.appwide.NotificationService
import net.tactware.nimbus.gitrepos.dal.GitBranchesRepository
import net.tactware.nimbus.gitrepos.dal.GitReposRepository
import org.eclipse.jgit.api.Git
import org.koin.core.annotation.Single
import java.io.File

/**
 * Use case for switching branches in git repositories.
 */
@Single
class SwitchBranchUseCase(
    private val gitReposRepository: GitReposRepository,
    private val gitBranchesRepository: GitBranchesRepository
) {
    /**
     * Switches to a different branch in a git repository.
     *
     * @param repoId The ID of the repository
     * @param branchName The name of the branch to switch to
     * @return True if the branch was switched successfully, false otherwise
     */
    suspend fun switchBranch(repoId: Long, branchName: String): Boolean = withContext(Dispatchers.Default) {
        try {
            // Get the repository by ID
            val repo = gitReposRepository.getRepoById(repoId)
            if (repo == null) {
                NotificationService.addNotification(
                    title = "Error",
                    message = "Repository not found"
                )
                return@withContext false
            }

            // Check if the repository has a clone path
            val repoPath = repo.clonePath
            if (repoPath == null) {
                NotificationService.addNotification(
                    title = "Error",
                    message = "Repository path is null"
                )
                return@withContext false
            }

            // Open the repository and execute the checkout command
            val git = Git.open(File(repoPath))
            try {
                // Execute the checkout command
                git.checkout()
                    .setName(branchName)
                    .call()

                // Update the in-memory repository
                gitBranchesRepository.setCurrentBranch(repoId, branchName)

                return@withContext true
            } catch (e: Exception) {
                // Show notification with error message
                NotificationService.addNotification(
                    title = "Checkout Failed",
                    message = "Failed to checkout branch: ${e.message}"
                )
                return@withContext false
            } finally {
                git.close()
            }
        } catch (e: Exception) {
            // Show notification with error message
            NotificationService.addNotification(
                title = "Error",
                message = "Error switching branch: ${e.message}"
            )
            return@withContext false
        }
    }
}
