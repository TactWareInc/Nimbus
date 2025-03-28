package net.tactware.nimbus

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import net.tactware.nimbus.appwide.ui.App
import net.tactware.nimbus.appwide.ui.theme.DesktopTheme
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

fun main() = application {
    startKoin {
        loadKoinModules(NimbusModule().module)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "Nimbus", // Updated title to match the app name
        state = WindowState(size = DpSize(1280.dp, 800.dp)) // Set a default window size that works well for desktop
    ) {
        // Use the desktop-specific theme
        DesktopTheme {
            App()
        }
    }
}
