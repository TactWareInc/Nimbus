package net.tactware.nimbus.projects.bl

import net.tactware.nimbus.projects.dal.ProjectsRepository
import net.tactware.nimbus.projects.dal.entities.DevOpsServerOrService
import net.tactware.nimbus.projects.dal.entities.Project
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class SaveProjectUseCase(private val projectsRepository: ProjectsRepository) {

    suspend fun invoke(
        projectName: String,
        projectUrl : String,
        isDevOpsServer : DevOpsServerOrService,
        personalAccessToken: String,
        id : String? = null
    ) {
        projectsRepository.storeProject(
            Project(
                id = id ?: Uuid.random().toString(),
                name = projectName,
                projectUrl = projectUrl,
                isServerOrService = isDevOpsServer,
                personalAccessToken = personalAccessToken,
            )
        )
    }
}