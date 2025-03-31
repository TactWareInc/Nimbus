package net.tactware.nimbus.appwide.bl

import kotlinx.serialization.Serializable

@Serializable
data class PoolResponse(
    val count: Int,
    val value: List<Value>
)