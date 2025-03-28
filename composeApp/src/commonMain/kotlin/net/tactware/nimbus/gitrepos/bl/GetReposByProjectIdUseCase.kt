package net.tactware.nimbus.gitrepos.bl

import kotlinx.coroutines.flow.Flow
import net.tactware.nimbus.gitrepos.dal.GitRepo
import net.tactware.nimbus.gitrepos.dal.GitReposRepository
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class GetReposByProjectIdUseCase(private val gitReposRepository: GitReposRepository) {
    suspend operator fun invoke(projectId: Uuid): Flow<List<GitRepo>> {
        return gitReposRepository.getReposByProjectId(projectId)
    }
}