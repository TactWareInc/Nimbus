package net.tactware.nimbus.projects.dal.entities


data class Project(
    val id: String,
    val url: String,
    val name: String,
    val isServerOrService: DevOpsServerOrService,
    val personalAccessToken: String,
)
