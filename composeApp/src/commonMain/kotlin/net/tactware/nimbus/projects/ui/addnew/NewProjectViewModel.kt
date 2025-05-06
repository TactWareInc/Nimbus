package net.tactware.nimbus.projects.ui.addnew

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.NotificationService
import net.tactware.nimbus.appwide.bl.AzureDevOpsClient
import net.tactware.nimbus.projects.bl.PatExpirationChecker
import net.tactware.nimbus.projects.bl.SaveProjectUseCase
import net.tactware.nimbus.projects.dal.entities.DevOpsServerOrService
import net.tactware.nimbus.projects.dal.entities.PatInfo
import net.tactware.nimbus.projects.dal.entities.Project
import org.koin.core.annotation.Factory

@Factory
class NewProjectViewModel(
    private val saveProjectUseCase: SaveProjectUseCase,
    private val patExpirationChecker: PatExpirationChecker
) : ViewModel() {

    var projectLocalName by mutableStateOf("")

    var isDevOpsServerOrService by mutableStateOf(DevOpsServerOrService.SERVICE)

    var projectUrl by mutableStateOf("")

    var process by mutableStateOf(ProcessType.AGILE)

    var personalAccessToken by mutableStateOf("")

    var patExpirationDate by mutableStateOf<Long?>(null)

    // PAT information
    var patInfo by mutableStateOf<PatInfo?>(null)
    var isLoadingPatInfo by mutableStateOf(false)
    var patInfoError by mutableStateOf<String?>(null)

    val saveAccessible = derivedStateOf {
        projectLocalName.isNotEmpty() && projectUrl.isNotEmpty() && personalAccessToken.isNotEmpty()
    }

    internal

    fun onInteraction(interaction: NewProjectInteractions) {
        when (interaction) {
            is NewProjectInteractions.NameProject -> {
                projectLocalName = interaction.name
            }

            is NewProjectInteractions.SetIsServerOrService -> {
                isDevOpsServerOrService = interaction.isServer
            }

            is NewProjectInteractions.UrlProject -> {
                projectUrl = interaction.url
                if (projectLocalName.isEmpty()) {
                    projectLocalName = interaction.url.substringAfterLast("/")
                }
            }

            NewProjectInteractions.SaveProject -> {
                println("[DEBUG_LOG] NewProjectViewModel: Saving project $projectLocalName ($projectUrl)")
                viewModelScope.launch(Dispatchers.Default) {
                    // Save the project
                    println("[DEBUG_LOG] NewProjectViewModel: Calling SaveProjectUseCase for project $projectLocalName")
                    when(saveProjectUseCase.invoke(
                        projectName = projectLocalName,
                        projectUrl = projectUrl,
                        isDevOpsServer = isDevOpsServerOrService,
                        personalAccessToken = personalAccessToken,
                        patExpirationDate = patExpirationDate
                    )){
                        SaveProjectUseCase.UseCaseResult.SUCCESS ->{
                            println("[DEBUG_LOG] NewProjectViewModel: Project $projectLocalName saved successfully")
                            projectLocalName = ""
                            projectUrl = ""
                            personalAccessToken = ""
                            patExpirationDate = null
                            patInfo = null
                            patInfoError = null
                            isDevOpsServerOrService
                        }
                        SaveProjectUseCase.UseCaseResult.FAILURE ->{
                            println("[DEBUG_LOG] NewProjectViewModel: Failed to save project $projectLocalName")
                            NotificationService.addNotification(
                                title = "Failed to save project",
                                message = "An error occurred while saving the project. Please try again.",
                            )
                        }
                        SaveProjectUseCase.UseCaseResult.ALREADY_EXISTS ->{
                            println("[DEBUG_LOG] NewProjectViewModel: Project $projectLocalName already exists")
                            NotificationService.addNotification(
                                title = "Project already exists",
                                message = "A project with the same URL already exists. Please choose a different URL.",
                            )
                        }
                    }
                }
            }

            is NewProjectInteractions.PAT -> {
                personalAccessToken = interaction.personalAccessToken

                // Only try to fetch PAT info if we have both a URL and a PAT
                if (projectUrl.isNotEmpty() && personalAccessToken.isNotEmpty()) {
                    fetchPatInfo()
                } else {
                    // Clear PAT info if PAT is empty
                    patInfo = null
                    patExpirationDate = null
                    patInfoError = null
                }
            }

            is NewProjectInteractions.PATExpiration -> {
                patExpirationDate = interaction.expirationDate
            }
        }
    }


    enum class ProcessType(val displayName: String) {
        AGILE("Agile"),
        SCRUM("Scrum"),
        CMMI("CMMI"),
        BASIC("Basic"),
        CUSTOM("Custom");
    }

    /**
     * Fetches information about the PAT from the Azure DevOps API.
     */
    private fun fetchPatInfo() {
        isLoadingPatInfo = true
        patInfoError = null

        viewModelScope.launch(Dispatchers.Default) {
            try {
                // Create a temporary project object to use with the PAT checker
                val tempProject = Project(
                    id = "",
                    projectUrl = projectUrl,
                    name = projectLocalName,
                    isServerOrService = isDevOpsServerOrService,
                    personalAccessToken = personalAccessToken
                )

                // Query PAT information
                val info = patExpirationChecker.queryPatInfo(tempProject)

                // Update the view model state
                patInfo = info
                patExpirationDate = info?.validTo
                patInfoError = null

                // Log the result
                println("[DEBUG_LOG] NewProjectViewModel: PAT info retrieved: $info")

                if (info?.isExpired() == true) {
                    NotificationService.addNotification(
                        title = "PAT Expired",
                        message = "The Personal Access Token you entered has expired. Please provide a valid PAT.",
                    )
                } else if (info?.isExpiring() == true) {
                    NotificationService.addNotification(
                        title = "PAT Expiring Soon",
                        message = "The Personal Access Token you entered will expire soon. Consider generating a new one.",
                    )
                }
            } catch (e: Exception) {
                println("[DEBUG_LOG] NewProjectViewModel: Error fetching PAT info: ${e.message}")
                patInfoError = "Failed to retrieve PAT information: ${e.message}"
                patInfo = null
            } finally {
                isLoadingPatInfo = false
            }
        }
    }
}
