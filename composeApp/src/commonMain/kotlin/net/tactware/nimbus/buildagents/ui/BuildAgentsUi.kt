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
        // Header
        Text(
            "Build Agents",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
        )

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
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
                    ) {
                        items(agents) { agent ->
                            BuildAgentCard(agent)
                        }
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
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    agent.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    agent.description ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Status indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .padding(end = MaterialTheme.spacing.small),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.size(12.dp),
                    shape = RoundedCornerShape(6.dp),
                    color = if (agent.isOnline) Color.Green else Color.Red
                ) {}
            }
            
            Text(
                if (agent.isOnline) "Online" else "Offline",
                style = MaterialTheme.typography.bodyMedium,
                color = if (agent.isOnline) Color.Green else Color.Red
            )
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