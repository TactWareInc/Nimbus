package net.tactware.nimbus.projects.ui.addnew

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import net.tactware.nimbus.projects.dal.entities.DevOpsServerOrService
import org.koin.core.annotation.Factory

@Factory
class NewProjectViewModel : ViewModel() {

    var projectLocalName by mutableStateOf("")

    var isDevOpsServerOrService by mutableStateOf(DevOpsServerOrService.SERVICE)

    var projectUrl by mutableStateOf("")

    var process by mutableStateOf(ProcessType.AGILE)

    var personalAccessToken by mutableStateOf("")

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
                // Save the project
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
