package app.frontend

import dev.fritz2.dom.html.RenderContext
import kotlinx.browser.document
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

fun RenderContext.fritzLinks() {
    navSection("more about fritz2")
    navLink {
        text("Homepage")
    }
    navLink {
        text("Documentation")
    }
    navLink {
        text("API")
    }

    navSection("other Examples")
    navLink {
        text("Components")
    }
    navLink {
        text("Examples")
    }

}