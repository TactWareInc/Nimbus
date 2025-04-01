package net.tactware.nimbus.buildagents.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import net.tactware.nimbus.appwide.ui.theme.spacing
import org.koin.compose.viewmodel.koinViewModel

/**
 * UI component for displaying and managing build agents.
 */
@Composable
fun BuildAgentsUi() {
    val viewModel = koinViewModel<BuildAgentsViewModel>()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.medium)
    ) {
        // Header with title and refresh button
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Build Agents",
                style = MaterialTheme.typography.headlineMedium
            )

            Button(
                onClick = { viewModel.refreshAgents() },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Refresh Agents")
            }
        }

        // Build agents list
        when (uiState) {
            is BuildAgentsViewModel.UiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is BuildAgentsViewModel.UiState.Error -> {
                val errorState = uiState as BuildAgentsViewModel.UiState.Error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Error: ${errorState.message}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))
                        Button(onClick = { viewModel.refreshAgents() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            is BuildAgentsViewModel.UiState.Success -> {
                val agents = (uiState as BuildAgentsViewModel.UiState.Success).agents

                if (agents.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No build agents found",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    // Separate agents into online and offline
                    val onlineAgents = agents.filter { it.isOnline }
                    val offlineAgents = agents.filter { !it.isOnline }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
                    ) {
                        // Online Agents Card
                        AgentCategoryCard(
                            title = "Online Agents",
                            agents = onlineAgents,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer
                        )

                        // Offline Agents Card
                        AgentCategoryCard(
                            title = "Offline Agents",
                            agents = offlineAgents,
                            backgroundColor = MaterialTheme.colorScheme.errorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BuildAgentCard(agent: BuildAgent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = agent.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = agent.description ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

            // Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(10.dp),
                        shape = RoundedCornerShape(5.dp),
                        color = if (agent.isOnline) Color.Green else Color.Red
                    ) {}
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = if (agent.isOnline) "Online" else "Offline",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (agent.isOnline) Color.Green else Color.Red
                )
            }
        }
    }
}

/**
 * Composable function for displaying a category of agents (online or offline) in a card.
 */
@Composable
fun AgentCategoryCard(
    title: String,
    agents: List<BuildAgent>,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.medium)
        ) {
            // Card title
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
            )

            // List of agent names
            if (agents.isEmpty()) {
                Text(
                    text = "No agents in this category",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 200.dp) // Limit height to ensure both cards are visible
                ) {
                    items(agents) { agent ->
                        AgentListItem(agent)
                    }
                }
            }
        }
    }
}

/**
 * Composable function for displaying an individual agent in the list.
 */
@Composable
fun AgentListItem(agent: BuildAgent) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium, vertical = MaterialTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Agent name and description
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = agent.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                agent.description?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Status indicator
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(8.dp),
                        shape = RoundedCornerShape(4.dp),
                        color = if (agent.isOnline) Color.Green else Color.Red
                    ) {}
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = if (agent.isOnline) "Online" else "Offline",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (agent.isOnline) Color.Green else Color.Red
                )
            }
        }
    }
}

/**
 * Data class representing a build agent.
 */
data class BuildAgent(
    val id: String,
    val name: String,
    val description: String? = null,
    val isOnline: Boolean = false
)
