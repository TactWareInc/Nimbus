package net.tactware.nimbus

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import net.tactware.nimbus.appwide.ui.App
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

fun main() = application {
    startKoin {
        loadKoinModules(NimbusModule().module)
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "KotlinProject",
    ) {
        App()
    }
}