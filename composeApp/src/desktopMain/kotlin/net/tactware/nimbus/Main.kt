package net.tactware.nimbus

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import net.tactware.nimbus.appwide.ui.App
import org.koin.core.context.startKoin

fun main() = application {
    startKoin {

    }
    Window(
        onCloseRequest = ::exitApplication,
        title = "KotlinProject",
    ) {
        App()
    }
}