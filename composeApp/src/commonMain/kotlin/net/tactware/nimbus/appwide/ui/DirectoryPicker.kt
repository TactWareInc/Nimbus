package net.tactware.nimbus.appwide.ui

/**
 * Interface for platform-specific directory picker functionality.
 */
interface DirectoryPicker {
    /**
     * Opens a directory picker dialog and returns the selected directory path.
     * 
     * @param title The title of the directory picker dialog
     * @return The selected directory path, or null if the user cancelled the dialog
     */
    suspend fun pickDirectory(title: String): String?
}