package net.tactware.nimbus.projects.bl

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.tactware.nimbus.appwide.bl.AzureDevOpsClient
import net.tactware.nimbus.gitrepos.dal.GitReposRepository
import net.tactware.nimbus.projects.dal.entities.Project
import net.tactware.nimbus.projects.dal.entities.azurejson.repo.ProjectRepoInformation
import org.koin.core.annotation.Single
import java.io.File
import kotlin.uuid.Uuid

@Single
class ProjectUpdater(
    private val getProjectByIdUseCase: GetProjectByIdUseCase,
    private val gitReposRepository: GitReposRepository
) {
    suspend fun update(projectId: Uuid) {
        val project = getProjectByIdUseCase.invoke(projectId)
        if (project != null) {
            val client = AzureDevOpsClient(project)

            updateRepositories(client, project)
        }
    }

    private suspend fun updateRepositories(client: AzureDevOpsClient, project: Project) {
        val listOfRepos = Json.parseToJsonElement(client.getProjectRepositories()).jsonObject["value"]
        val repoInfos = listOfRepos?.let {
            Json.decodeFromString<List<ProjectRepoInformation>>( it.toString())
        } ?: emptyList()

        for (repoInfo in repoInfos) {
                gitReposRepository.storeRepo(repoInfo.webUrl, repoInfo.name, project.id)
        }

        // Check to see if local repositories are still valid
        gitReposRepository.getReposByProjectIdList(Uuid.parse(project.id)).filter { it.isCloned }.forEach {
            val doesPathStillExist = it.clonePath?.let { File(it).exists() } == true
            if (!doesPathStillExist) {
                gitReposRepository.updateCloneStatus(null, false, it.id)
            }
        }
    }
}