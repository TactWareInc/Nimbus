package net.tactware.nimbus.projects.dal.entities

import net.tactware.nimbus.projects.ui.addnew.NewProjectInteractions

data class Project(
    val url: String,
    val name: String,
    val isServerOrService: DevOpsServerOrService,
    val personalAccessToken: String,
    )
