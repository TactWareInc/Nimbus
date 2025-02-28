package net.tactware.nimbus.projects.dal.entities

import kotlin.uuid.Uuid

data class ProjectIdentifier(
    val id : Uuid,
    val name : String,
)