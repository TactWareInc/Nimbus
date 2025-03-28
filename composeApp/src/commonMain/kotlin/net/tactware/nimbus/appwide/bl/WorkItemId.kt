package net.tactware.nimbus.appwide.bl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("workItem")
data class WorkItemId(
    val id: Int,
    val url: String
)