package net.tactware.nimbus.appwide.bl

import kotlinx.serialization.Serializable

@Serializable
data class Owner(
    val descriptor: String,
    val displayName: String,
    val id: String,
    val imageUrl: String,
    val uniqueName: String,
    val url: String
)