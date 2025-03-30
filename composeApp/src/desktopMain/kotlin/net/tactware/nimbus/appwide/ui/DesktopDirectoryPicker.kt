package net.tactware.nimbus.appwide.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

/**
 * Desktop implementation of the DirectoryPicker interface.
 * Uses platform-specific file dialogs to provide a native directory picker experience.
 */
@Single
class DesktopDirectoryPicker : DirectoryPicker {

    /**
     * Opens a directory picker dialog using the most appropriate native system dialog
     * for the current platform and returns the selected directory path.
     * 
     * @param title The title of the directory picker dialog
     * @return The selected directory path, or null if the user cancelled the dialog
     */
    override suspend fun pickDirectory(title: String): String? = withContext(Dispatchers.IO) {
        // Detect the operating system
        val os = System.getProperty("os.name").lowercase()

        // Use different approaches based on the OS
        when {
            // On macOS, use FileDialog with the special property for directory selection
            os.contains("mac") -> {
                try {
                    // Set the property to allow directory selection
                    System.setProperty("apple.awt.fileDialogForDirectories", "true")

                    // Create and show the dialog
                    val fileDialog = FileDialog(null as Frame?, title, FileDialog.LOAD)
                    fileDialog.isVisible = true

                    // Reset the property
                    System.setProperty("apple.awt.fileDialogForDirectories", "false")

                    // Return the selected path or null
                    if (fileDialog.directory != null && fileDialog.file != null) {
                        File(fileDialog.directory, fileDialog.file).absolutePath
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    // Fallback to JFileChooser if there's an error
                    useJFileChooser(title)
                } finally {
                    // Ensure the property is reset even if an exception occurs
                    System.setProperty("apple.awt.fileDialogForDirectories", "false")
                }
            }

            // On Windows and Linux, JFileChooser often provides better directory selection
            else -> {
                useJFileChooser(title)
            }
        }
    }

    /**
     * Fallback method that uses JFileChooser for directory selection.
     * 
     * @param title The title of the directory picker dialog
     * @return The selected directory path, or null if the user cancelled the dialog
     */
    private fun useJFileChooser(title: String): String? {
        val fileChooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory).apply {
            dialogTitle = title
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            isAcceptAllFileFilterUsed = false
        }

        val result = fileChooser.showOpenDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile.absolutePath
        } else {
            null
        }
    }
}
