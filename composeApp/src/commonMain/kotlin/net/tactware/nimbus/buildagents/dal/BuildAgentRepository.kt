package net.tactware.nimbus.buildagents.dal

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.tactware.nimbus.projects.dal.entities.Project
import org.koin.core.annotation.Single

/**
 * Repository for managing build agent data in the database.
 * This allows the app to store and retrieve build agent status even when the app is closed.
 * 
 * Note: This is a simplified implementation until SQLDelight code generation runs.
 * It will be replaced with a full implementation that uses the database.
 */
@Single
class BuildAgentRepository {
    private val json = Json { ignoreUnknownKeys = true }

    // In-memory cache of build agents until SQLDelight is set up
    private val buildAgentsCache = mutableMapOf<Int, BuildAgentWithMetadata>()

    // Flow of all build agents for reactive UI updates
    private val _buildAgentsFlow = MutableStateFlow<List<BuildAgentInfo>>(emptyList())
    val buildAgentsFlow = _buildAgentsFlow.asStateFlow()

    /**
     * Store a build agent in the repository.
     * 
     * @param agent The build agent to store
     * @param projectId The ID of the project the agent belongs to
     */
    suspend fun storeBuildAgent(agent: BuildAgentInfo, projectId: String) {
        buildAgentsCache[agent.id] = BuildAgentWithMetadata(
            agent = agent,
            projectId = projectId,
            lastUpdated = System.currentTimeMillis()
        )
        updateBuildAgentsFlow()
    }

    /**
     * Store multiple build agents in the repository.
     * 
     * @param agents The build agents to store
     * @param projectId The ID of the project the agents belong to
     */
    suspend fun storeBuildAgents(agents: List<BuildAgentInfo>, projectId: String) {
        agents.forEach { storeBuildAgent(it, projectId) }
    }

    /**
     * Get all build agents for a project.
     * 
     * @param projectId The ID of the project
     * @return A list of build agents
     */
    suspend fun getBuildAgentsForProject(projectId: String): List<BuildAgentInfo> {
        return buildAgentsCache.values
            .filter { it.projectId == projectId }
            .map { it.agent }
    }

    /**
     * Get a build agent by ID.
     * 
     * @param agentId The ID of the build agent
     * @return The build agent, or null if not found
     */
    suspend fun getBuildAgentById(agentId: Int): BuildAgentInfo? {
        return buildAgentsCache[agentId]?.agent
    }

    /**
     * Get build agents by status.
     * 
     * @param status The status to filter by
     * @param projectId The ID of the project
     * @return A list of build agents with the given status
     */
    suspend fun getBuildAgentsByStatus(status: String, projectId: String): List<BuildAgentInfo> {
        return buildAgentsCache.values
            .filter { it.projectId == projectId && it.agent.status.equals(status, ignoreCase = true) }
            .map { it.agent }
    }

    /**
     * Delete a build agent.
     * 
     * @param agentId The ID of the build agent to delete
     */
    suspend fun deleteBuildAgent(agentId: Int) {
        buildAgentsCache.remove(agentId)
        updateBuildAgentsFlow()
    }

    /**
     * Delete all build agents for a project.
     * 
     * @param projectId The ID of the project
     */
    suspend fun deleteBuildAgentsForProject(projectId: String) {
        buildAgentsCache.entries.removeIf { it.value.projectId == projectId }
        updateBuildAgentsFlow()
    }

    /**
     * Get the last known status of a build agent.
     * 
     * @param agentId The ID of the build agent
     * @return A pair of the status and the timestamp of when it was last updated, or null if not found
     */
    suspend fun getBuildAgentStatus(agentId: Int): Pair<String, Long>? {
        return buildAgentsCache[agentId]?.let {
            Pair(it.agent.status, it.lastUpdated)
        }
    }

    /**
     * Get a flow of all build agents for reactive UI updates.
     * 
     * @return A flow of all build agents
     */
    fun getAllBuildAgentsFlow(): Flow<List<BuildAgentInfo>> {
        return buildAgentsFlow
    }

    /**
     * Get all build agents.
     * 
     * @return A list of all build agents
     */
    suspend fun getAllBuildAgents(): List<BuildAgentInfo> {
        return buildAgentsCache.values.map { it.agent }
    }

    /**
     * Update the build agents flow with the current cache.
     */
    private fun updateBuildAgentsFlow() {
        _buildAgentsFlow.value = buildAgentsCache.values.map { it.agent }
    }

    /**
     * Data class to store a build agent with metadata.
     */
    private data class BuildAgentWithMetadata(
        val agent: BuildAgentInfo,
        val projectId: String,
        val lastUpdated: Long
    )
}
