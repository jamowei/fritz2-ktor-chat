package app.frontend

import dev.fritz2.binding.SimpleHandler
import dev.fritz2.dom.html.RenderContext
import kotlinx.browser.document
import kotlinx.browser.window
import navLink
import navSection
import org.w3c.dom.HTMLTextAreaElement

fun copyToClipboard(text: String) {
    document.body?.let { body ->
        (document.createElement("textarea") as HTMLTextAreaElement).apply {
            value = text
            setAttribute("readonly", "")
            style.position = "absolute"
            style.left = "-9999px"
            body.appendChild(this)
            select()
            setSelectionRange(0, 999999)
            document.execCommand("copy")
            body.removeChild(this)
        }
    }
}

val ChatStore.jumpTo: SimpleHandler<String>
    get() = handle { chat, url ->
        window.open(url, "_blank")
        chat
    }

fun RenderContext.fritzLinks() {
    navSection("more about fritz2")
    navLink {
        text("Homepage")
    }.map { "https://fritz2.dev" } handledBy ChatStore.jumpTo
    navLink {
        text("Documentation")
    }.map { "https://docs.fritz2.dev" } handledBy ChatStore.jumpTo
    navLink {
        text("API")
    }.map { "https://api.fritz2.dev" } handledBy ChatStore.jumpTo

    navSection("other Examples")
    navLink {
        text("Components")
    }.map { "https://components.fritz2.dev" } handledBy ChatStore.jumpTo
    navLink {
        text("Examples")
    }.map { "https://fritz2.dev/examples.html" } handledBy ChatStore.jumpTo
}

fun registerServiceWorker() {
    try {
        window.addEventListener("load", {
            window.navigator.serviceWorker.register("/serviceWorker.js")
        })
        console.log("ServiceWorker registered")
    } catch (t: Throwable) {
        console.log("Error registering ServiceWorker:", t)
    }
}
