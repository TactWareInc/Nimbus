package net.tactware.nimbus.projects.dal.entities.azurejson

import kotlinx.serialization.Serializable

@Serializable
data class ProjectRepoInformation(
    val defaultBranch: String,
    val id: String,
    val isDisabled: Boolean,
    val name: String,
    val project: ProjectDetails,
    val remoteUrl: String,
    val size: Int,
    val url: String,
    val webUrl: String
)