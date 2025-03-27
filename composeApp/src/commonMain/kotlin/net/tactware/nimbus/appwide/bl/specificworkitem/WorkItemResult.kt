package net.tactware.nimbus.appwide.bl.specificworkitem

import kotlinx.serialization.Serializable

@Serializable
data class WorkItemResult(
    val count : Int,
    val value : List<WorkItemDetails>
)