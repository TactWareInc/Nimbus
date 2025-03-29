package net.tactware.nimbus.appwide.ui

/**
 * Interface for platform-specific browser launching functionality.
 */
interface BrowserLauncher {
    /**
     * Opens a URL in the default browser.
     * 
     * @param url The URL to open
     * @return True if the URL was opened successfully, false otherwise
     */
    suspend fun openUrl(url: String): Boolean
}