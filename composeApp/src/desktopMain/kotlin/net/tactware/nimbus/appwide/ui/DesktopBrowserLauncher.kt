package net.tactware.nimbus.appwide.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.awt.Desktop
import java.net.URI

/**
 * Desktop implementation of the BrowserLauncher interface.
 * Uses Java AWT Desktop to open URLs in the default browser.
 */
@Single
class DesktopBrowserLauncher : BrowserLauncher {
    
    /**
     * Opens a URL in the default browser using Desktop.browse().
     * 
     * @param url The URL to open
     * @return True if the URL was opened successfully, false otherwise
     */
    override suspend fun openUrl(url: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI(url))
                true
            } else {
                println("Desktop browsing not supported")
                false
            }
        } catch (e: Exception) {
            println("Error opening URL: ${e.message}")
            false
        }
    }
}