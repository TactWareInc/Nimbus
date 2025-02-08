package net.tactware.nimbus.projects.dal

import net.tactware.nimbus.Projects
import net.tactware.nimbus.db.NimbusDb
import net.tactware.nimbus.projects.dal.entities.DevOpsServerOrService
import net.tactware.nimbus.projects.dal.entities.Project

class ProjectsRepository(private val database: NimbusDb) {
    private val queries = database.projectsQueries

    private val mapper: (Projects) -> Project = {
        Project(
            it.id,
            it.name,
            if (it.isAzureDevopsServer) DevOpsServerOrService.SERVER else DevOpsServerOrService.SERVICE,
            it.personalAccessToken
        )
    }

    suspend fun storeProject(project: Project) {
        queries.storeProject(
            project.url,
            project.name,
            project.isServerOrService == DevOpsServerOrService.SERVER,
            project.personalAccessToken
        )
    }

    suspend fun getProjects(): List<Project> {
        return queries.getAllProjects().executeAsList().map(mapper)
    }

    suspend fun getAllProjectsNames(): List<String> {
        return queries.getAllProjectNames().executeAsList()
    }

    suspend fun getProjectByName(name: String): Project? {
        return queries.getProjectByName(name).executeAsOneOrNull()?.let {
            mapper.invoke(it)
        }
    }
}