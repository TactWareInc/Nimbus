package net.tactware.nimbus.projects.ui.specific

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import net.tactware.nimbus.appwide.bl.AzureDevOpsClient
import net.tactware.nimbus.projects.bl.GetProjectByIdUseCase
import net.tactware.nimbus.projects.bl.ProjectUpdater
import net.tactware.nimbus.projects.dal.entities.azurejson.ProjectRepoInformation
import org.koin.core.annotation.Factory
import org.koin.core.annotation.InjectedParam
import kotlin.uuid.Uuid

@Factory
class SpecificProjectViewModel(
    @InjectedParam
    projectId: Uuid,
    projectUpdater: ProjectUpdater
) : ViewModel() {

    init {
        viewModelScope.launch {
            projectUpdater.update(projectId)
        }
    }
}