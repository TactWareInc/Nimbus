package net.tactware.nimbus.appwide.bl

import kotlinx.serialization.Serializable

@Serializable
data class Value(
    val agentCloudId: String?,
    val autoProvision: Boolean,
    val autoSize: Boolean,
    val autoUpdate: Boolean,
    val createdBy: CreatedBy,
    val createdOn: String,
    val id: Int,
    val isHosted: Boolean,
    val isLegacy: Boolean,
    val name: String,
    val options: String,
    val owner: Owner,
    val poolType: String,
    val scope: String,
    val size: Int,
    val targetSize: String?,
)