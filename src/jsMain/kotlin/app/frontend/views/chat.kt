package app.frontend.views

import app.frontend.ChatStore
import app.shared.Chat
import app.shared.ChatMessage
import app.shared.L
import dev.fritz2.components.*
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.params.styled
import kotlinx.browser.document
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.w3c.dom.HTMLTextAreaElement

@ExperimentalCoroutinesApi
fun RenderContext.chatMessage(msg: ChatMessage, self: Boolean) {
    val bgColor = if (self) "#94c2ed" else "#86bb71"

    lineUp({
        margins { bottom { normal } }
        textAlign { if (self) right else left }
        fontSize { small }
    }) {
        reversed(self)
        spacing { smaller }
        items {
            if (!self) {
                icon({
                    size { normal }
                    margins { top { "0.1rem" } }
                }) { fromTheme { user } }
            }
            (::span.styled {
                fontWeight { semiBold }
            }) {
                +msg.member
            }
            (::span.styled {
                color { "#a8aab1" }
            }) {
                icon({
                    size { small }
                    margins { right { tiny } }
                }) { fromTheme { clock } }
                +msg.created.print()
            }
        }
    }
    (::div.styled {
        boxShadow { flat }
        background { color { bgColor } }
        color { "white" }
        paddings {
            vertical { smaller }
            horizontal { small }
        }
        lineHeight { huge }
        fontSize { normal }
        margins { bottom { larger } }
        width { "90%" }
        position { relative { } }
        margins {
            if (self) left { "10%" } else right { "10%" }
        }
        radius { normal }
        after {
            border {
                style { solid }
                width { "10px" }
                color { "transparent" }
            }
            borders { bottom { color { bgColor } } }
            position {
                absolute {
                    bottom { "100%" }
                    if (self) right { large } else left { large }
                }
            }
            height { none }
            width { none }
            css("""content: " "; pointer-events: none;""")
        }
    }) {
        +msg.content
    }
}

@ExperimentalCoroutinesApi
fun RenderContext.chatPage() {
    (::ul.styled {
        css("list-style: none;")
    }) {
        ChatStore.sub(L.Chat.messages).data.renderEach { msg ->
            li { chatMessage(msg, (msg.member == ChatStore.current.member)) }
        }
    }
}

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