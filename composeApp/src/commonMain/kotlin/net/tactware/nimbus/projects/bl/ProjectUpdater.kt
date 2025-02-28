package net.tactware.nimbus.projects.bl

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.tactware.nimbus.appwide.bl.AzureDevOpsClient
import net.tactware.nimbus.projects.dal.entities.Project
import net.tactware.nimbus.projects.dal.entities.ProjectIdentifier
import net.tactware.nimbus.projects.dal.entities.azurejson.ProjectRepoInformation
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class ProjectUpdater(
    private val getProjectByIdUseCase: GetProjectByIdUseCase
) {

    suspend fun update(projectId: Uuid) {
        val project = getProjectByIdUseCase.invoke(projectId)
        if (project != null) {
            val client = AzureDevOpsClient(project)

            updateRepositories(client)
        }
    }

    private suspend fun updateRepositories(client: AzureDevOpsClient) {
        val listOfRepos = Json.parseToJsonElement(client.getProjectRepositories()).jsonObject["value"]
        listOfRepos?.let {
            val repos = Json.decodeFromString<List<ProjectRepoInformation>>( it.toString())
            println(repos)
        }
    }
}