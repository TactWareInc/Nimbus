package net.tactware.nimbus.buildagents.dal

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing the response from the Azure DevOps API for build agents.
 */
@Serializable
data class BuildAgentResponse(
    val count: Int,
    val value: List<BuildAgentInfo>
)

/**
 * Data class representing a build agent from the Azure DevOps API.
 */
@Serializable
data class BuildAgentInfo(
    val id: Int,
    val name: String,
    val status: String,
    val enabled: Boolean,
    @SerialName("systemCapabilities")
    val capabilities: Map<String, String>? = null,
    val version: String? = null,
    val osDescription: String? = null
)