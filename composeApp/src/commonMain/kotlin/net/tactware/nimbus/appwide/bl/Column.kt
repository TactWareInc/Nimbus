package net.tactware.nimbus.appwide.bl

import kotlinx.serialization.Serializable

@Serializable
data class Column(
    val name: String,
    val referenceName: String,
    val url: String
)