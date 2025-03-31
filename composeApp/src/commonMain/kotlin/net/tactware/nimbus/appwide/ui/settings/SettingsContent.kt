package net.tactware.nimbus.appwide.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.tactware.nimbus.appwide.ui.theme.spacing
import net.tactware.nimbus.buildagents.ui.BuildAgentsUi
import net.tactware.nimbus.projects.ui.customfields.CustomFieldsManagerUi
import org.koin.compose.viewmodel.koinViewModel

/**
 * Main content for the Settings page.
 * This follows a similar structure to the Projects page with a master-detail view.
 */
@Composable
fun SettingsContent() {
    val viewModel = koinViewModel<SettingsViewModel>()

    // Collect state values
    val selectedCategoryIndex by viewModel.selectedCategoryIndex.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // Use a Row layout for list pane detail scaffold
    Row(Modifier.fillMaxSize()) {
        // Left pane: Settings categories list (30% width)
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.3f)
                .padding(MaterialTheme.spacing.small),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.small)
            ) {
                // Header
                Text(
                    "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(MaterialTheme.spacing.small)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                // Settings categories list
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(SettingsViewModel.allCategories) { category ->
                        // Find the index of the category
                        val index = SettingsViewModel.allCategories.indexOf(category)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(MaterialTheme.spacing.small)
                                .clickable {
                                    viewModel.onInteraction(SettingsViewInteractions.SelectCategory(category))
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedCategoryIndex == index) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(MaterialTheme.spacing.medium)
                            ) {
                                Text(
                                    category.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Right pane: Settings category details (70% width)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.7f)
                .padding(MaterialTheme.spacing.small)
        ) {
            when (uiState) {
                is SettingsViewModel.UiState.CategorySelected -> {
                    val category = (uiState as? SettingsViewModel.UiState.CategorySelected)?.category

                    // Show the appropriate settings content based on the selected category
                    if (category != null) {
                        when (category) {
                            SettingsCategory.WorkItemSettings -> {
                                Card(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.medium)
                                    ) {
                                        Text(
                                            category.title,
                                            style = MaterialTheme.typography.headlineMedium,
                                            modifier = Modifier.padding(bottom = MaterialTheme.spacing.medium)
                                        )

                                        // Custom Fields Manager
                                        CustomFieldsManagerUi()
                                    }
                                }
                            }
                            SettingsCategory.BuildAgentsSettings -> {
                                Card(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    BuildAgentsUi()
                                }
                            }
                        }
                        // Add more categories here as needed
                    } else {
                        // Handle the case where category is null
                        Card(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.medium),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    "No settings category selected",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
