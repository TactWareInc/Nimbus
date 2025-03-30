package net.tactware.nimbus.gitrepos.bl

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Factory

/**
 * Use case for getting the IDs of repositories that are currently being downloaded.
 * This allows the ViewModel to observe downloading repositories without directly depending on the singleton.
 */
@Factory
class GetDownloadingReposUseCase(
    private val repositoryDownloadTracker: RepositoryDownloadTracker
) {
    /**
     * Returns a flow of repository IDs that are currently being downloaded.
     * 
     * @return Flow of repository IDs
     */
    operator fun invoke(): Flow<Set<Long>> {
        return repositoryDownloadTracker.downloadingRepoIds
    }
}