package net.tactware.nimbus.appwide.bl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WiqlReturn(
    val asOf: String,
    val columns: List<Column>,
    val queryResultType: String,
    val queryType: String,
    @SerialName("workItems")
    val workItemIds: List<WorkItemId>
)