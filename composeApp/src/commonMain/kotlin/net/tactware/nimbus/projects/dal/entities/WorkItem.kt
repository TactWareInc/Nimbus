package net.tactware.nimbus.projects.dal.entities

/**
 * Represents a work item from Azure DevOps.
 */
data class WorkItem(
    val id: Int,
    val title: String,
    val description : String,
    val state: String,
    val assignedTo: String,
    val type: String
)