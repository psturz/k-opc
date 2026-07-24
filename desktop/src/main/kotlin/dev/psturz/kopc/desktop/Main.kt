package dev.psturz.kopc.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.psturz.kopc.ui.ApiClient
import dev.psturz.kopc.ui.App

fun main() {
    val api = ApiClient()

    application {
        Window(onCloseRequest = { api.close(); exitApplication() }, title = "k-opc") {
            App(api)
        }
    }
}
