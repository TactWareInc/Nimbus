package net.tactware.nimbus.projects.bl

import net.tactware.nimbus.projects.dal.ProjectsRepository
import net.tactware.nimbus.projects.dal.entities.Project
import org.koin.core.annotation.Factory
import kotlin.uuid.Uuid

@Factory
class GetProjectByIdUseCase(private val projectRepository: ProjectsRepository) {
    suspend operator fun invoke(projectId: Uuid): Project? {
        return projectRepository.getProject(projectId)
    }
}