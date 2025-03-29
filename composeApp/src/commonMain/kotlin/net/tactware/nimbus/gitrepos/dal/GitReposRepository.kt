package net.tactware.nimbus.gitrepos.dal

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import migrations.net.tactware.nimbus.GitRepos
import net.tactware.nimbus.appwide.dal.IDatabaseProvider
import net.tactware.nimbus.db.NimbusDb
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class GitReposRepository(provider: IDatabaseProvider<NimbusDb>) {
    private val database = provider.database
    private val queries = provider.database.repositoriesQueries
    private val joinQueries = provider.database.projectstogitreposQueries

    private val mapper: (GitRepos) -> GitRepo = {
        with(it){
            GitRepo(
                id = id,
                name = name,
                url = url,
            )
        }
    }

    suspend fun storeRepo(gitUrl : String, gitRepoName : String, projectId: String) {
        database.transaction {
            if(!queries.checkIfProjectExistsByURL(gitUrl).executeAsOne()) {
                queries.storeGitRepo(
                    name = gitRepoName,
                    url = gitUrl,
                    is_cloned = false,
                    clone_path = null
                )
                val gitRepoId = queries.lastRowInserted().executeAsOne()
                joinQueries.storeProjectToGitRepo(projectId, gitRepoId)
            }
        }
    }

    suspend fun getReposByProjectId(projectId: Uuid): Flow<List<GitRepo>> {
        return queries.getAllGitReposForProject(projectId.toString()).asFlow().mapToList(Dispatchers.Default).map {
            it.map(mapper)
        }
    }

    suspend fun checkIfRepoExists(gitUrl: String): Boolean {
        return queries.checkIfProjectExistsByURL(gitUrl).executeAsOne()
    }

    suspend fun updateCloneStatus(clonePath: String?, isCloned: Boolean, repoId : Long) {
        queries.updateGitRepoCloneStatus(isCloned, clonePath, repoId)
    }
}