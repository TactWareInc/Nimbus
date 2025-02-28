package net.tactware.nimbus.appwide.bl

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json

/**
 * A client class for interacting with Azure DevOps services.
 *
 * @param url The base URL for the Azure DevOps services.
 * @param project The name of the project within the organization.
 * @param team The name of the team within the project.
 * @property collectionOrOrganization The name of the collection or organization within Azure DevOps.
 * @property pat The Personal Access Token used for authentication.
 * @constructor Creates an AzureDevOpsClient instance to interact with Azure DevOps services.
 */
class AzureDevOpsClient(
    private val projectUrl: String,
    private val pat: String
) {
    companion object {
        /**
         * The API version used for Azure DevOps REST API requests.
         */
        private const val API_VERSION = "7.0"
    }

    private val orgOrCollection : String
        get() {
            val parts = projectUrl.split("/")
            return if (projectUrl.endsWith("/")) {
                parts[parts.size - 3]
            } else {
                parts[parts.size - 2]
            }
        }

    // HTTP client configured for making REST API calls
    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
        install(Auth) {
            basic {
                credentials {
                    BasicAuthCredentials(
                        username = projectUrl,
                        password = pat
                    )
                }
            }
        }
    }

    suspend fun getProjectRepositories(project: String) {
        val response = client.post {
            url("$url/_apis/git/repositories")
            headers {
                append("Content-Type", "application/json")
            }
            setBody()
        }
    }
}