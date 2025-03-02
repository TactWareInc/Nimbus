package net.tactware.nimbus.projects.dal.entities.azurejson.repo

import kotlinx.serialization.Serializable

@Serializable
data class ProjectDetails(
    val description: String,
    val id: String,
    val lastUpdateTime: String,
    val name: String,
    val revision: Int,
    val state: String,
    val url: String,
    val visibility: String
)