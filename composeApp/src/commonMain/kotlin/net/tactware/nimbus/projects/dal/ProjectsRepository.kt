package net.tactware.nimbus.projects.dal

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.all
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import net.tactware.nimbus.Projects
import net.tactware.nimbus.appwide.dal.IDatabaseProvider
import net.tactware.nimbus.db.NimbusDb
import net.tactware.nimbus.projects.dal.entities.DevOpsServerOrService
import net.tactware.nimbus.projects.dal.entities.Project
import org.koin.core.annotation.Single
import kotlin.uuid.Uuid

@Single
class ProjectsRepository(provider: IDatabaseProvider<NimbusDb>) {
    private val queries = provider.database.projectsQueries

    private val projectDataFlow =
        queries.getAllProjects().asFlow().mapToList(Dispatchers.Default).map { it.map(mapper) }
            .stateIn(CoroutineScope(Dispatchers.Default), SharingStarted.WhileSubscribed(), emptyList())


    private val mapper: (Projects) -> Project = {
        Project(
            id = it.id,
            url = it.projectUrl,
            name = it.name,
            isServerOrService = if (it.isAzureDevopsServer) DevOpsServerOrService.SERVER else DevOpsServerOrService.SERVICE,
            personalAccessToken = it.personalAccessToken
        )
    }

    suspend fun storeProject(project: Project) {
        queries.storeProject(
            id = project.id,
            name = project.name,
            projectUrl = project.url,
            isAzureDevopsServer = project.isServerOrService == DevOpsServerOrService.SERVER,
            personalAccessToken = project.personalAccessToken,
            projectProcessType = "AGILE"
        )
    }

    suspend fun getProjects(): List<Project> {
        return queries.getAllProjects().executeAsList().map(mapper)
    }

    fun getAllProjectsFlow() = projectDataFlow

    suspend fun getAllProjectsNames(): List<String> {
        return queries.getAllProjectNames().executeAsList()
    }

    suspend fun getProjectByName(name: String): Project? {
        return queries.getProjectByName(name).executeAsOneOrNull()?.let {
            mapper.invoke(it)
        }
    }
}