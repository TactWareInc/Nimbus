package net.tactware.nimbus.appwide.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import javax.swing.JFileChooser
import javax.swing.filechooser.FileSystemView

/**
 * Desktop implementation of the DirectoryPicker interface.
 * Uses Java Swing JFileChooser to open a directory picker dialog.
 */
@Single
class DesktopDirectoryPicker : DirectoryPicker {
    
    /**
     * Opens a directory picker dialog using JFileChooser and returns the selected directory path.
     * 
     * @param title The title of the directory picker dialog
     * @return The selected directory path, or null if the user cancelled the dialog
     */
    override suspend fun pickDirectory(title: String): String? = withContext(Dispatchers.IO) {
        val fileChooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory).apply {
            this.dialogTitle = title
            this.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            this.isAcceptAllFileFilterUsed = false
        }
        
        val result = fileChooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile.absolutePath
        } else {
            null
        }
    }
}