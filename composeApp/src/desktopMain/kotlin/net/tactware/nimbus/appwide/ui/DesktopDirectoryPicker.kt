package net.tactware.nimbus.appwide.ui

import net.tactware.nimbus.appwide.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.awt.Color
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.io.FilenameFilter
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileSystemView

/**
 * Desktop implementation of the DirectoryPicker interface.
 * Uses platform-specific file dialogs to provide a native directory picker experience.
 * Respects the system's light/dark mode setting.
 */
@Single
class DesktopDirectoryPicker : DirectoryPicker {

    // Dark mode colors
    private val darkBackground = Color(43, 43, 43)
    private val darkForeground = Color(220, 220, 220)

    // Light mode colors
    private val lightBackground = Color(240, 240, 240)
    private val lightForeground = Color(0, 0, 0)


    /**
     * Applies the appropriate theme to Swing components based on dark mode detection.
     * Comprehensive styling for JFileChooser and related components.
     * 
     * @param isDarkMode whether to apply dark mode styling
     */
    private fun applyThemeToSwing(isDarkMode: Boolean) {
        if (isDarkMode) {
            // Dark theme colors
            val darkBorder = Color(60, 60, 60)
            val darkSelection = Color(75, 110, 175)
            val darkSelectionText = Color(255, 255, 255)

            // Apply dark theme to Swing components
            // Basic components
            UIManager.put("Panel.background", darkBackground)
            UIManager.put("Panel.foreground", darkForeground)

            // Text components
            UIManager.put("TextField.background", darkBackground)
            UIManager.put("TextField.foreground", darkForeground)
            UIManager.put("TextField.caretForeground", darkForeground)
            UIManager.put("TextArea.background", darkBackground)
            UIManager.put("TextArea.foreground", darkForeground)
            UIManager.put("TextArea.caretForeground", darkForeground)

            // Buttons
            UIManager.put("Button.background", darkBackground)
            UIManager.put("Button.foreground", darkForeground)
            UIManager.put("Button.select", darkSelection)
            UIManager.put("Button.focus", darkSelection)

            // Labels
            UIManager.put("Label.background", darkBackground)
            UIManager.put("Label.foreground", darkForeground)

            // Lists and tables
            UIManager.put("List.background", darkBackground)
            UIManager.put("List.foreground", darkForeground)
            UIManager.put("List.selectionBackground", darkSelection)
            UIManager.put("List.selectionForeground", darkSelectionText)
            UIManager.put("Table.background", darkBackground)
            UIManager.put("Table.foreground", darkForeground)
            UIManager.put("Table.selectionBackground", darkSelection)
            UIManager.put("Table.selectionForeground", darkSelectionText)
            UIManager.put("Table.gridColor", darkBorder)

            // Scroll bars
            UIManager.put("ScrollBar.background", darkBackground)
            UIManager.put("ScrollBar.thumb", Color(80, 80, 80))
            UIManager.put("ScrollBar.thumbDarkShadow", darkBorder)
            UIManager.put("ScrollBar.thumbHighlight", Color(100, 100, 100))
            UIManager.put("ScrollBar.thumbShadow", Color(70, 70, 70))
            UIManager.put("ScrollBar.track", darkBackground)

            // File chooser specific
            UIManager.put("FileChooser.background", darkBackground)
            UIManager.put("FileChooser.foreground", darkForeground)
            UIManager.put("FileChooser.listBackground", darkBackground)
            UIManager.put("FileChooser.listForeground", darkForeground)
            UIManager.put("FileChooser.detailsViewBackground", darkBackground)
            UIManager.put("FileChooser.detailsViewForeground", darkForeground)
            UIManager.put("FileChooser.viewMenuBackground", darkBackground)
            UIManager.put("FileChooser.viewMenuForeground", darkForeground)
            UIManager.put("FileChooser.newFolderIcon", null) // Use default icon
            UIManager.put("FileChooser.upFolderIcon", null) // Use default icon
            UIManager.put("FileChooser.homeFolderIcon", null) // Use default icon
            UIManager.put("FileChooser.detailsViewIcon", null) // Use default icon
            UIManager.put("FileChooser.listViewIcon", null) // Use default icon

            // Additional FileChooser components
            UIManager.put("FileChooser.ancestorInputMap", null)
            UIManager.put("FileChooser.cancelButtonText", "Cancel")
            UIManager.put("FileChooser.cancelButtonToolTipText", "Abort file chooser dialog")
            UIManager.put("FileChooser.directoryOpenButtonText", "Open")
            UIManager.put("FileChooser.directoryOpenButtonToolTipText", "Open selected directory")
            UIManager.put("FileChooser.fileNameLabelText", "File name:")
            UIManager.put("FileChooser.filesOfTypeLabelText", "Files of type:")
            UIManager.put("FileChooser.lookInLabelText", "Look in:")
            UIManager.put("FileChooser.openButtonText", "Open")
            UIManager.put("FileChooser.openButtonToolTipText", "Open selected file")
            UIManager.put("FileChooser.saveButtonText", "Save")
            UIManager.put("FileChooser.saveButtonToolTipText", "Save selected file")
            UIManager.put("FileChooser.updateButtonText", "Update")
            UIManager.put("FileChooser.updateButtonToolTipText", "Update directory listing")
            UIManager.put("FileChooser.helpButtonText", "Help")
            UIManager.put("FileChooser.helpButtonToolTipText", "FileChooser help")
            UIManager.put("FileChooser.acceptAllFileFilterText", "All Files")

            // Tree components (used in file chooser)
            UIManager.put("Tree.background", darkBackground)
            UIManager.put("Tree.foreground", darkForeground)
            UIManager.put("Tree.selectionBackground", darkSelection)
            UIManager.put("Tree.selectionForeground", darkSelectionText)
            UIManager.put("Tree.selectionBorderColor", darkSelection)
            UIManager.put("Tree.hash", darkForeground)
            UIManager.put("Tree.line", darkForeground)
            UIManager.put("Tree.textBackground", darkBackground)
            UIManager.put("Tree.textForeground", darkForeground)

            // ComboBox components (used in file chooser)
            UIManager.put("ComboBox.background", darkBackground)
            UIManager.put("ComboBox.foreground", darkForeground)
            UIManager.put("ComboBox.selectionBackground", darkSelection)
            UIManager.put("ComboBox.selectionForeground", darkSelectionText)
            UIManager.put("ComboBox.buttonBackground", darkBackground)
            UIManager.put("ComboBox.buttonDarkShadow", darkBorder)
            UIManager.put("ComboBox.buttonHighlight", Color(100, 100, 100))
            UIManager.put("ComboBox.buttonShadow", Color(70, 70, 70))

            // ToolTip components
            UIManager.put("ToolTip.background", darkBackground)
            UIManager.put("ToolTip.foreground", darkForeground)
            UIManager.put("ToolTip.border", darkBorder)

            // Borders
            UIManager.put("Separator.foreground", darkBorder)
            UIManager.put("Separator.background", darkBackground)
            UIManager.put("TitledBorder.titleColor", darkForeground)
        } else {
            // Light theme colors
            val lightBorder = Color(180, 180, 180)
            val lightSelection = Color(184, 207, 229)
            val lightSelectionText = Color(0, 0, 0)

            // Apply light theme to Swing components
            // Basic components
            UIManager.put("Panel.background", lightBackground)
            UIManager.put("Panel.foreground", lightForeground)

            // Text components
            UIManager.put("TextField.background", lightBackground)
            UIManager.put("TextField.foreground", lightForeground)
            UIManager.put("TextField.caretForeground", lightForeground)
            UIManager.put("TextArea.background", lightBackground)
            UIManager.put("TextArea.foreground", lightForeground)
            UIManager.put("TextArea.caretForeground", lightForeground)

            // Buttons
            UIManager.put("Button.background", lightBackground)
            UIManager.put("Button.foreground", lightForeground)
            UIManager.put("Button.select", lightSelection)
            UIManager.put("Button.focus", lightSelection)

            // Labels
            UIManager.put("Label.background", lightBackground)
            UIManager.put("Label.foreground", lightForeground)

            // Lists and tables
            UIManager.put("List.background", lightBackground)
            UIManager.put("List.foreground", lightForeground)
            UIManager.put("List.selectionBackground", lightSelection)
            UIManager.put("List.selectionForeground", lightSelectionText)
            UIManager.put("Table.background", lightBackground)
            UIManager.put("Table.foreground", lightForeground)
            UIManager.put("Table.selectionBackground", lightSelection)
            UIManager.put("Table.selectionForeground", lightSelectionText)
            UIManager.put("Table.gridColor", lightBorder)

            // Scroll bars
            UIManager.put("ScrollBar.background", lightBackground)
            UIManager.put("ScrollBar.thumb", Color(200, 200, 200))
            UIManager.put("ScrollBar.thumbDarkShadow", lightBorder)
            UIManager.put("ScrollBar.thumbHighlight", Color(230, 230, 230))
            UIManager.put("ScrollBar.thumbShadow", Color(180, 180, 180))
            UIManager.put("ScrollBar.track", lightBackground)

            // File chooser specific
            UIManager.put("FileChooser.background", lightBackground)
            UIManager.put("FileChooser.foreground", lightForeground)
            UIManager.put("FileChooser.listBackground", lightBackground)
            UIManager.put("FileChooser.listForeground", lightForeground)
            UIManager.put("FileChooser.detailsViewBackground", lightBackground)
            UIManager.put("FileChooser.detailsViewForeground", lightForeground)
            UIManager.put("FileChooser.viewMenuBackground", lightBackground)
            UIManager.put("FileChooser.viewMenuForeground", lightForeground)
            UIManager.put("FileChooser.newFolderIcon", null) // Use default icon
            UIManager.put("FileChooser.upFolderIcon", null) // Use default icon
            UIManager.put("FileChooser.homeFolderIcon", null) // Use default icon
            UIManager.put("FileChooser.detailsViewIcon", null) // Use default icon
            UIManager.put("FileChooser.listViewIcon", null) // Use default icon

            // Additional FileChooser components
            UIManager.put("FileChooser.ancestorInputMap", null)
            UIManager.put("FileChooser.cancelButtonText", "Cancel")
            UIManager.put("FileChooser.cancelButtonToolTipText", "Abort file chooser dialog")
            UIManager.put("FileChooser.directoryOpenButtonText", "Open")
            UIManager.put("FileChooser.directoryOpenButtonToolTipText", "Open selected directory")
            UIManager.put("FileChooser.fileNameLabelText", "File name:")
            UIManager.put("FileChooser.filesOfTypeLabelText", "Files of type:")
            UIManager.put("FileChooser.lookInLabelText", "Look in:")
            UIManager.put("FileChooser.openButtonText", "Open")
            UIManager.put("FileChooser.openButtonToolTipText", "Open selected file")
            UIManager.put("FileChooser.saveButtonText", "Save")
            UIManager.put("FileChooser.saveButtonToolTipText", "Save selected file")
            UIManager.put("FileChooser.updateButtonText", "Update")
            UIManager.put("FileChooser.updateButtonToolTipText", "Update directory listing")
            UIManager.put("FileChooser.helpButtonText", "Help")
            UIManager.put("FileChooser.helpButtonToolTipText", "FileChooser help")
            UIManager.put("FileChooser.acceptAllFileFilterText", "All Files")

            // Tree components (used in file chooser)
            UIManager.put("Tree.background", lightBackground)
            UIManager.put("Tree.foreground", lightForeground)
            UIManager.put("Tree.selectionBackground", lightSelection)
            UIManager.put("Tree.selectionForeground", lightSelectionText)
            UIManager.put("Tree.selectionBorderColor", lightSelection)
            UIManager.put("Tree.hash", lightForeground)
            UIManager.put("Tree.line", lightForeground)
            UIManager.put("Tree.textBackground", lightBackground)
            UIManager.put("Tree.textForeground", lightForeground)

            // ComboBox components (used in file chooser)
            UIManager.put("ComboBox.background", lightBackground)
            UIManager.put("ComboBox.foreground", lightForeground)
            UIManager.put("ComboBox.selectionBackground", lightSelection)
            UIManager.put("ComboBox.selectionForeground", lightSelectionText)
            UIManager.put("ComboBox.buttonBackground", lightBackground)
            UIManager.put("ComboBox.buttonDarkShadow", lightBorder)
            UIManager.put("ComboBox.buttonHighlight", Color(230, 230, 230))
            UIManager.put("ComboBox.buttonShadow", Color(180, 180, 180))

            // ToolTip components
            UIManager.put("ToolTip.background", lightBackground)
            UIManager.put("ToolTip.foreground", lightForeground)
            UIManager.put("ToolTip.border", lightBorder)

            // Borders
            UIManager.put("Separator.foreground", lightBorder)
            UIManager.put("Separator.background", lightBackground)
            UIManager.put("TitledBorder.titleColor", lightForeground)
        }
    }

    /**
     * Opens a directory picker dialog using the most appropriate native system dialog
     * for the current platform and returns the selected directory path.
     * The dialog respects the system's light/dark mode setting.
     * 
     * @param title The title of the directory picker dialog
     * @return The selected directory path, or null if the user cancelled the dialog
     */
    override suspend fun pickDirectory(title: String): String? = withContext(Dispatchers.IO) {
        // Get the current theme mode from the application
        val isDarkMode = isAppInDarkTheme()

        // Apply theme to Swing components
        applyThemeToSwing(isDarkMode)

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

                    // Apply theme to FileDialog (limited support in AWT)
                    if (isDarkMode) {
                        fileDialog.background = darkBackground
                        fileDialog.foreground = darkForeground
                    }

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
                    useJFileChooser(title, isDarkMode)
                } finally {
                    // Ensure the property is reset even if an exception occurs
                    System.setProperty("apple.awt.fileDialogForDirectories", "false")
                }
            }

            // On Windows and Linux, JFileChooser often provides better directory selection
            else -> {
                useJFileChooser(title, isDarkMode)
            }
        }
    }

    /**
     * Fallback method that uses JFileChooser for directory selection.
     * Applies the appropriate theme based on the system's light/dark mode.
     * 
     * @param title The title of the directory picker dialog
     * @param isDarkMode Whether to apply dark mode styling
     * @return The selected directory path, or null if the user cancelled the dialog
     */
    private fun useJFileChooser(title: String, isDarkMode: Boolean): String? {
        // Apply theme to Swing components
        applyThemeToSwing(isDarkMode)

        val fileChooser = JFileChooser(FileSystemView.getFileSystemView().homeDirectory).apply {
            dialogTitle = title
            fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
            isAcceptAllFileFilterUsed = false

            // Apply theme to the file chooser
            if (isDarkMode) {
                background = darkBackground
                foreground = darkForeground
            } else {
                background = lightBackground
                foreground = lightForeground
            }
        }

        val result = fileChooser.showOpenDialog(null)
        return if (result == JFileChooser.APPROVE_OPTION) {
            fileChooser.selectedFile.absolutePath
        } else {
            null
        }
    }
}
