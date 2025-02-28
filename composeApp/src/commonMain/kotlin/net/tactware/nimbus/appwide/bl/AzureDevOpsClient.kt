package net.tactware.nimbus.appwide.bl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import net.tactware.nimbus.projects.dal.entities.Project
import org.koin.core.annotation.Factory
import org.koin.core.annotation.Single

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

}