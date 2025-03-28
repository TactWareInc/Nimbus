package net.tactware.nimbus.projects.dal.entities.azurejson.workitem

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WorkItemResponse(
    val count: Int,
    val value: List<WorkItemValue>
)

@Serializable
data class WorkItemValue(
    val id: Int,
    val fields: WorkItemFields
)

@Serializable
data class WorkItemFields(
    @SerialName("System.Title")
    val title: String,
    @SerialName("System.State")
    val state: String,
    @SerialName("System.AssignedTo")
    val assignedTo: String? = null
)
