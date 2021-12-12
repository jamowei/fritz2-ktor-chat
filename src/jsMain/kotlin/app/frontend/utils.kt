package app.frontend

import dev.fritz2.binding.SimpleHandler
import dev.fritz2.components.icon
import dev.fritz2.components.lineUp
import dev.fritz2.dom.Listener
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.h3
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLTextAreaElement
import org.w3c.dom.events.MouseEvent

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

fun RenderContext.navSection(text: String) {
    h3({
        paddings {
            vertical { "0.5rem" }
            horizontal { small }
        }
        margins { top { small } }
        textTransform { uppercase }
        fontWeight { semiBold }
        fontSize { ".8rem" }
        color { gray400 }
    }) { +text }
}

fun RenderContext.navLink(text: String): Listener<MouseEvent> {

    var clickEvents: Listener<MouseEvent>? = null

    lineUp({
        css("cursor: pointer")
        paddings {
            vertical { "0.6rem" }
            horizontal { small }
        }
        alignItems { center }
        borders {
            left {
                width { "0.2rem" }
                color { "transparent" }
            }
        }
        children(" .icon") {
            size { large }
            margins {
                left { tiny }
            }
        }
        children(" a") {
            display { block }
            fontWeight { "500" }
            fontSize { ".9rem" }
        }
    }) {
        spacing { small }
        items {
            icon { fromTheme { bookmark } }
            a { +text }
        }
        events {
            clickEvents = clicks.stopImmediatePropagation()
        }
    }

    return clickEvents!!
}

fun RenderContext.fritzLinks() {
    navSection("more about fritz2")
    navLink("Homepage").map { "https://fritz2.dev" } handledBy ChatStore.jumpTo
    navLink("Documentation").map { "https://docs.fritz2.dev" } handledBy ChatStore.jumpTo
    navLink("API").map { "https://api.fritz2.dev" } handledBy ChatStore.jumpTo

    navSection("other Examples")
    navLink("Components").map { "https://components.fritz2.dev" } handledBy ChatStore.jumpTo
    navLink("Examples").map { "https://fritz2.dev/examples.html" } handledBy ChatStore.jumpTo
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
