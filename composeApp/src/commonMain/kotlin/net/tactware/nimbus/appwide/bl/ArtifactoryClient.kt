package net.tactware.nimbus.appwide.bl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.tactware.nimbus.appwide.bl.LoggerStatic
import net.tactware.nimbus.projects.dal.entities.Project
import org.koin.core.annotation.Factory
import java.io.File
import java.io.FileOutputStream

/**
 * A client class for interacting with Azure DevOps Artifactory to download Universal packages.
 * This solves the problem of having to use Azure CLI to download Universal packages from ADO.
 *
 * @param project The Project containing connection details for Azure DevOps.
 */
class ArtifactoryClient(

    private val project: Project,
) {
    companion object {
        /**
         * The API version used for Azure DevOps REST API requests.
         */
        private const val API_VERSION = "7.0-preview"
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

    /**
     * Lists all Universal packages in a feed.
     *
     * @param feedId The ID of the feed containing the packages.
     * @return A list of packages in the feed.
     */
    suspend fun listPackages(feedId: String): List<Package> {
        val url = "${project.orgOrCollectionUrl}/_apis/packaging/feeds/$feedId/packages?api-version=$API_VERSION"

        val response = client.get(url) {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }

        if (!response.status.isSuccess()) {
            LoggerStatic.e("ArtifactoryClient", "Failed to list packages: ${response.status}")
            return emptyList()
        }

        val json = Json { ignoreUnknownKeys = true }
        val packageResponse = json.decodeFromString<PackageResponse>(response.body())
        return packageResponse.value
    }

    /**
     * Lists all versions of a specific package in a feed.
     *
     * @param feedId The ID of the feed containing the package.
     * @param packageId The ID of the package.
     * @return A list of package versions.
     */
    suspend fun listPackageVersions(feedId: String, packageId: String): List<PackageVersion> {
        val url = "${project.orgOrCollectionUrl}/_apis/packaging/feeds/$feedId/packages/$packageId/versions?api-version=$API_VERSION"

        val response = client.get(url) {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }

        if (!response.status.isSuccess()) {
            LoggerStatic.e("ArtifactoryClient", "Failed to list package versions: ${response.status}")
            return emptyList()
        }

        val json = Json { ignoreUnknownKeys = true }
        val versionResponse = json.decodeFromString<PackageVersionResponse>(response.body())
        return versionResponse.value
    }

    /**
     * Downloads a specific version of a Universal package.
     *
     * @param feedId The ID of the feed containing the package.
     * @param packageId The ID of the package.
     * @param packageVersion The version of the package to download.
     * @param destinationPath The local path where the package should be saved.
     * @return True if the download was successful, false otherwise.
     */
    suspend fun downloadPackage(
        feedId: String,
        packageId: String,
        packageVersion: String,
        destinationPath: String
    ): Boolean {
        val url = "${project.orgOrCollectionUrl}/_apis/packaging/feeds/$feedId/upack/packages/$packageId/versions/$packageVersion/content?api-version=$API_VERSION"

        val response: HttpResponse = client.get(url) {
            headers {
                append(HttpHeaders.ContentType, "application/json")
            }
        }

        if (!response.status.isSuccess()) {
            LoggerStatic.e("ArtifactoryClient", "Failed to download package: ${response.status}")
            return false
        }

        return saveResponseToFile(response, destinationPath)
    }

    /**
     * Saves the response body to a file.
     *
     * @param response The HTTP response containing the file data.
     * @param destinationPath The path where the file should be saved.
     * @return True if the file was saved successfully, false otherwise.
     */
    private suspend fun saveResponseToFile(response: HttpResponse, destinationPath: String): Boolean {
        return try {
            val channel: ByteReadChannel = response.bodyAsChannel()
            val file = File(destinationPath)

            // Create parent directories if they don't exist
            file.parentFile?.mkdirs()

            // Convert ByteReadChannel to InputStream and copy to file
            channel.toInputStream().use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            true
        } catch (e: Exception) {
            LoggerStatic.e("ArtifactoryClient", "Error saving file: ${e.message}")
            false
        }
    }
}

/**
 * Data class representing a package in Azure DevOps Artifactory.
 */
@Serializable
data class Package(
    val id: String,
    val name: String,
    val protocolType: String,
    val url: String
)

/**
 * Data class representing a response containing a list of packages.
 */
@Serializable
data class PackageResponse(
    val count: Int,
    val value: List<Package>
)

/**
 * Data class representing a package version in Azure DevOps Artifactory.
 */
@Serializable
data class PackageVersion(
    val id: String,
    val normalizedVersion: String,
    val version: String,
    val isLatest: Boolean,
    val publishDate: String
)

/**
 * Data class representing a response containing a list of package versions.
 */
@Serializable
data class PackageVersionResponse(
    val count: Int,
    val value: List<PackageVersion>
)
