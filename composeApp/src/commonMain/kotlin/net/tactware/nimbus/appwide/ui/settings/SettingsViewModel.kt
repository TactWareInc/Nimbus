package net.tactware.nimbus.appwide.ui.settings

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.tactware.nimbus.appwide.ui.settings.SettingsCategory.WorkItemSettings
import org.koin.core.annotation.Factory

/**
 * Sealed class representing different types of settings categories.
 */
sealed class SettingsCategory(val title: String) {
    object WorkItemSettings : SettingsCategory("Work Item Settings")
    object BuildAgentsSettings : SettingsCategory("Build Agents")
    // Add more settings categories here as needed
    // For example:
    // object GeneralSettings : SettingsCategory("General Settings")
    // object AppearanceSettings : SettingsCategory("Appearance Settings")
}

/**
 * Sealed class representing different interactions with the settings view.
 */
sealed class SettingsViewInteractions {
    data class SelectCategory(val category: SettingsCategory) : SettingsViewInteractions()
}

/**
 * ViewModel for the Settings page.
 */
@Factory
class SettingsViewModel : ViewModel() {

    /**
     * Sealed class representing different UI states for the settings page.
     */
    sealed class UiState {
        data class CategorySelected(val category: SettingsCategory) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.CategorySelected(SettingsCategory.WorkItemSettings))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _selectedCategoryIndex = MutableStateFlow(0)
    val selectedCategoryIndex: StateFlow<Int> = _selectedCategoryIndex.asStateFlow()

    /**
     * Handles interactions with the settings view.
     */
    fun onInteraction(interaction: SettingsViewInteractions) {
        when (interaction) {
            is SettingsViewInteractions.SelectCategory -> {
                _uiState.value = UiState.CategorySelected(interaction.category)
                _selectedCategoryIndex.value = allCategories.indexOf(interaction.category)
            }
        }
    }

    companion object {
        val allCategories = listOf(SettingsCategory.WorkItemSettings, SettingsCategory.BuildAgentsSettings)
    }
}
