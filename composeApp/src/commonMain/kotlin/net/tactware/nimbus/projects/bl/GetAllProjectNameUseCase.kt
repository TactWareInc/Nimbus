package net.tactware.nimbus.projects.bl

import net.tactware.nimbus.projects.dal.ProjectsRepository
import org.koin.core.annotation.Factory

@Factory
class GetAllProjectNameUseCase(private val projectsRepository: ProjectsRepository) {
    suspend operator fun invoke() = projectsRepository.getAllProjectsNames()
}