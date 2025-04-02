package net.tactware.nimbus.gitrepos.bl

import net.tactware.nimbus.gitrepos.dal.GitRepo
import org.koin.core.annotation.Factory

/**
 * Use case for creating a new branch in one or more repositories.
 * Delegates to the GitController for the actual branch creation.
 */
@Factory
class CreateBranchUseCase(
    private val gitController: GitController
) {
    /**
     * Creates a new branch in one or more repositories.
     * Delegates to the GitController for the actual branch creation.
     *
     * @param branchName The name of the branch to create
     * @param repos The repositories to create the branch in
     * @param pushToRemote Whether to push the branch to the remote repository
     * @param autoCheckout Whether to automatically check out the branch after creating it
     * @return True if the branch was created successfully in all repositories, false otherwise
     */
    suspend operator fun invoke(
        branchName: String,
        repos: List<GitRepo>,
        pushToRemote: Boolean = false,
        autoCheckout: Boolean = false
    ): Boolean {
        return gitController.createBranch(branchName, repos, pushToRemote, autoCheckout)
    }
}
