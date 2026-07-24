package dev.psturz.kopc.web

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import dev.psturz.kopc.ui.ApiClient
import dev.psturz.kopc.ui.App
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(document.body!!) {
        App(ApiClient())
    }
}
