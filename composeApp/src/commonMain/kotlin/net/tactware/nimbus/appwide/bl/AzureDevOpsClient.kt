package net.tactware.nimbus.appwide.bl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import net.tactware.nimbus.appwide.bl.specificworkitem.WorkItemResult
import net.tactware.nimbus.projects.dal.entities.Project
import net.tactware.nimbus.projects.dal.entities.WorkItem

/**
* A client class for interacting with Azure DevOps services.
*
* @param projectUrl The base URL for the Azure DevOps services.
* @param pat The Personal Access Token used for authentication.
*/
class AzureDevOpsClient(
    private val project: Project,
) {
    companion object {
        /**
         * The API version used for Azure DevOps REST API requests.
         */
        private const val API_VERSION = "7.0"

        private val MAX_QUERY_AMOUNT = 200
    }



    // HTTP client configured for making REST API calls
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(
                        username = "",
                        password = project.personalAccessToken
                    )
                }
            }
        }
    }

    private suspend fun get(url: String): String {
        return client.get(url) {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }.body()
    }

    private suspend fun wiqlQuery(wiqlUrl: String, query: String): String {
        val response = client.post {
            url(wiqlUrl)
            headers {
                append("Content-Type", "application/json")
            }
            this.setBody(mapOf("query" to query))
        }
        return response.body()
    }

    /**
     * Get project details.
     */
    suspend fun getProjectDetails(): String {
        val url = "${project.orgOrCollectionUrl}/_apis/projects/${project.projectName}?api-version=$API_VERSION"
        return get(url)
    }

    /**
     * Get repositories in the project.
     */
    suspend fun getProjectRepositories(): String {
        val url = "${project.projectUrl}/_apis/git/repositories?api-version=$API_VERSION"
        return get(url)
    }

    /**
     * Get teams in the project.
     */
    suspend fun getProjectTeams(): String {
        val url = "${project.projectUrl}/_apis/teams?api-version=$API_VERSION"
        return get(url)
    }

    /**
     * Get iterations and sprints for the team.
     */
    suspend fun getProjectIterations(team: String): String {
        val url = "${project.projectUrl}/$team/_apis/work/teamsettings/iterations?api-version=$API_VERSION"
        return get(url)
    }

    /**
     * Get area paths (work item hierarchy).
     */
    suspend fun getAreaPaths(): String {
        val url = "${project.projectUrl}_apis/wit/classificationnodes/areas?\$depth=2&api-version=$API_VERSION"
        return get(url)
    }

    /**
     * Get build definitions (CI/CD build pipelines).
     */
    suspend fun getBuildDefinitions(): String {
        val url = "${project.projectUrl}/_apis/build/definitions?api-version=$API_VERSION"
        return get(url)
    }

    /**
     * Retrieves work items by their IDs from Azure DevOps services.
     *
     * @param workItemIds A list of integers representing the work item IDs.
     * @return A string containing the work items data in JSON format.
     */
    private suspend fun getWorkItems(workItemIds: List<Int>): String {
        return client.get {
            url("${project.projectUrl}/_apis/wit/workitems/?ids=${workItemIds.joinToString(",")}&api-version=$API_VERSION&\$expand=all")
            headers {
                append("Content-Type", "application/json")
            }
        }.body<String>()
    }

    /**
     * Get work items for the project.
     * This method gets a list of work items from the project using a simple query.
     */
    suspend fun getWorkItems(): List<WorkItem> {
        val json = Json{ignoreUnknownKeys = true}
       val body = wiqlQuery(
            wiqlUrl = "${project.projectUrl}/_apis/wit/wiql?api-version=$API_VERSION",
            query = WorkItemQueries.items()
        )
        val result =  json.decodeFromString(WiqlReturn.serializer(), body)

        // Get the work items in batches of 200
        val workItems = mutableListOf<WorkItem>()
        val workItemIds = result.workItemIds.map { it.id }
        val batches = workItemIds.chunked(MAX_QUERY_AMOUNT)
        for (batch in batches) {
            val workItemsJson = getWorkItems(batch)
            val result = json.decodeFromString<WorkItemResult>(workItemsJson)

            workItems  += result.value.map {
               WorkItem(
                   id = it.id ?: 0,
                   title = it.fields?.systemTitle ?: "",
                   description = it.fields?.description?: "",
                   state = it.fields?.systemState ?: "",
                   assignedTo = it.fields?.systemAssignedTo?.displayName ?: "",
                   type = it.fields?.systemWorkItemType ?: ""
               )
            }
        }

        return workItems
    }
}
