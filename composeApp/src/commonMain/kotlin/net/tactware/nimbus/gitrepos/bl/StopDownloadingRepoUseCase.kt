package net.tactware.nimbus.gitrepos.bl

import org.koin.core.annotation.Factory

/**
 * Use case for stopping tracking a repository as being downloaded.
 */
@Factory
class StopDownloadingRepoUseCase(
    private val repositoryDownloadTracker: RepositoryDownloadTracker
) {
    /**
     * Stops tracking a repository as being downloaded.
     *
     * @param repoId The ID of the repository to stop tracking
     */
    operator fun invoke(repoId: Long) {
        repositoryDownloadTracker.stopDownloading(repoId)
    }
}