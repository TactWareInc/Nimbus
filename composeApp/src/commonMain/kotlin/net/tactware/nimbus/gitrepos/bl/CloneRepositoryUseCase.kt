package net.tactware.nimbus.gitrepos.bl

import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.projects.bl.GetProjectByIdUseCase
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import org.koin.core.annotation.Factory

/**
 * Use case for cloning a Git repository to a local directory using JGit.
 * Delegates to the RepositoryDownloadTracker for the actual cloning logic.
 */
@Factory
class CloneRepositoryUseCase(
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val repositoryDownloadTracker: RepositoryDownloadTracker
) {

    /**
     * Clones a Git repository to the specified directory using JGit.
     * 
     * @param repo The Git repository to clone
     * @param directory The directory to clone the repository to
     * @param projectIdentifier The identifier of the project that the repository belongs to
     * @return Result indicating success or failure with an error message
     */
    operator fun invoke(repo: GitRepo, directory: String, projectIdentifier: ProjectIdentifier? = null): Result<Unit> {
        // Delegate to the RepositoryDownloadTracker for the actual cloning logic
        return repositoryDownloadTracker.cloneRepository(
            repo = repo,
            directory = directory,
            projectIdentifier = projectIdentifier,
            getProjectByIdUseCase = getProjectByIdUseCase,
        )
    }
}
