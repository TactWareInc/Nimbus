package net.tactware.nimbus.buildagents.bl

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.tactware.nimbus.appwide.NotificationService
import net.tactware.nimbus.appwide.bl.AzureDevOpsClient
import net.tactware.nimbus.buildagents.dal.BuildAgentInfo
import net.tactware.nimbus.buildagents.dal.BuildAgentRepository
import net.tactware.nimbus.projects.dal.ProjectsRepository
import net.tactware.nimbus.projects.dal.entities.Project
import org.koin.core.annotation.Single

/**
 * Singleton for monitoring build agent status and sending notifications when agents come back online.
 * Uses BuildAgentRepository to store and retrieve build agent status even when the app is closed.
 */
@Single(createdAtStart = true)
class BuildAgentMonitor(
    private val projectsRepository: ProjectsRepository,
    private val buildAgentRepository: BuildAgentRepository
) {
    companion object{
        // Polling interval in milliseconds (default: 5 minutes)
        private const val POLLING_INTERVAL = 5 * 60 * 1000L
    }

    // Coroutine scope for background polling
    private val scope = CoroutineScope(Dispatchers.Default)

    // Job for the polling coroutine
    private var pollingJob: Job? = null

    // State flow to expose the current list of build agents
    val buildAgents: StateFlow<List<BuildAgentInfo>> = buildAgentRepository.buildAgentsFlow

    // Flag to indicate if monitoring is active
    private var isMonitoring = false

    init {
        CoroutineScope(Dispatchers.Default).launch {
            // Initialize the monitoring process
            startMonitoring()
        }
    }

    /**
     * Starts monitoring build agents for the given project.
     */
    fun startMonitoring() {
        if (isMonitoring) return

        isMonitoring = true
        pollingJob = scope.launch {
            while (isMonitoring) {
                try {
                    // Get the current project
                    val projects = projectsRepository.getProjects()
                    if (projects.isNotEmpty()) {
                        checkBuildAgentStatus(projects.first())
                    }
                } catch (e: Exception) {
                    // Log error but continue monitoring
                    println("Error in build agent monitoring: ${e.message}")
                }

                // Wait for the next polling interval
                delay(POLLING_INTERVAL)
            }
        }
    }

    /**
     * Stops monitoring build agents.
     */
    fun stopMonitoring() {
        isMonitoring = false
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * Checks the status of build agents for the given project and sends notifications for status changes.
     * 
     * @param project The project to check build agents for.
     */
    private suspend fun checkBuildAgentStatus(project: Project) {
        val client = AzureDevOpsClient(project)
        val currentAgents = client.getAllBuildAgents()

        // Store the current agents in the repository
        buildAgentRepository.storeBuildAgents(currentAgents, project.id)

        // Check for status changes
        for (agent in currentAgents) {
            val agentId = agent.id
            val isOnline = agent.status.equals("online", ignoreCase = true)

            // Get the previous status from the repository
            val previousStatus = buildAgentRepository.getBuildAgentStatus(agentId)
            val wasOnline = previousStatus?.let { 
                it.first.equals("online", ignoreCase = true) 
            } ?: false

            // If the agent was offline and is now online, send a notification
            if (!wasOnline && isOnline) {
                NotificationService.addNotification(
                    title = "Build Agent Online",
                    message = "Build agent '${agent.name}' is now online."
                )
            }
        }
    }

    /**
     * Forces an immediate check of build agent status.
     * 
     * @param project The project to check build agents for.
     */
    suspend fun forceCheck(project: Project) {
        checkBuildAgentStatus(project)
    }
}
