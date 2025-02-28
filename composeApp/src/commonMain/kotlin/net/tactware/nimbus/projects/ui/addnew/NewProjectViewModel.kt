package net.tactware.nimbus.projects.ui.addnew

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.tactware.nimbus.projects.bl.SaveProjectUseCase
import net.tactware.nimbus.projects.dal.entities.DevOpsServerOrService
import org.koin.core.annotation.Factory

@Factory
class NewProjectViewModel(private val saveProjectUseCase: SaveProjectUseCase) : ViewModel() {

    var projectLocalName by mutableStateOf("")

    var isDevOpsServerOrService by mutableStateOf(DevOpsServerOrService.SERVICE)

    var projectUrl by mutableStateOf("")

    var process by mutableStateOf(ProcessType.AGILE)

    var personalAccessToken by mutableStateOf("")

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
                viewModelScope.launch(Dispatchers.Default) {
                    // Save the project
                    saveProjectUseCase.invoke(
                        projectLocalName,
                        projectUrl,
                        isDevOpsServerOrService,
                        personalAccessToken,
                    )
                }
            }

            is NewProjectInteractions.PAT -> {
                personalAccessToken = interaction.personalAccessToken
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
}
