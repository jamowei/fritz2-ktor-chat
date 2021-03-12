package app.frontend

import kotlinx.browser.document
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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

fun Instant.print(): String =
    this.toLocalDateTime(TimeZone.currentSystemDefault())
        .let { "${it.hour}:${it.minute}" }