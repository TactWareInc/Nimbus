package net.tactware.nimbus.gitrepos.bl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.tactware.nimbus.gitrepos.dal.GitBranchesRepository
import net.tactware.nimbus.gitrepos.dal.GitReposRepository
import org.koin.core.annotation.Single

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
            // This is a placeholder. In a real implementation, you would need to
            // use a git library or execute git commands to switch branches.
            // For now, we'll just update the in-memory repository.
            gitBranchesRepository.setCurrentBranch(repoId, branchName)

            return@withContext true
        } catch (e: Exception) {
            // Log the error
            println("Error switching branch: ${e.message}")
            return@withContext false
        }
    }
}
