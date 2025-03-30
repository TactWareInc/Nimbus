package net.tactware.nimbus.appwide.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.StoredConfig
import org.koin.core.annotation.Factory
import java.io.File

/**
 * ViewModel for the Profile page.
 * Handles Git user configuration (name and email).
 */
@Factory
class ProfileViewModel : ViewModel() {
    // UI State
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadGitCredentials()
    }

    /**
     * Loads Git credentials from the global Git config.
     */
    private fun loadGitCredentials() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val config = Git.open(File(".")).repository.config
                val name = config.getString("user", null, "name") ?: ""
                val email = config.getString("user", null, "email") ?: ""

                _uiState.update { it.copy(
                    gitName = name,
                    gitEmail = email,
                    statusMessage = "",
                    isError = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    statusMessage = "Failed to load Git credentials: ${e.message ?: "Unknown error"}",
                    isError = true
                ) }
            }
        }
    }

    /**
     * Updates the Git user name in the UI state.
     */
    fun updateGitName(name: String) {
        _uiState.update { it.copy(gitName = name) }
    }

    /**
     * Updates the Git user email in the UI state.
     */
    fun updateGitEmail(email: String) {
        _uiState.update { it.copy(gitEmail = email) }
    }

    /**
     * Saves Git credentials to the global Git config.
     */
    fun saveGitCredentials() {
        val currentState = _uiState.value

        if (currentState.gitName.isBlank() || currentState.gitEmail.isBlank()) {
            _uiState.update { it.copy(
                statusMessage = "Name and email cannot be empty",
                isError = true
            ) }
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Try to open the Git repository in the current directory
                // This is just to get access to the config
                val git = Git.open(File("."))
                val config = git.repository.config

                // Set the user name and email in the global config
                config.setString("user", null, "name", currentState.gitName)
                config.setString("user", null, "email", currentState.gitEmail)
                config.save()

                _uiState.update { it.copy(
                    statusMessage = "Git credentials saved successfully",
                    isError = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    statusMessage = "Failed to save Git credentials: ${e.message ?: "Unknown error"}",
                    isError = true
                ) }
            }
        }
    }

    /**
     * Data class representing the UI state for the Profile page.
     */
    data class ProfileUiState(
        val gitName: String = "",
        val gitEmail: String = "",
        val statusMessage: String = "",
        val isError: Boolean = false
    )
}
