package app.frontend.views

import app.frontend.ChatStore
import app.shared.ChatMessage
import app.shared.L
import dev.fritz2.components.icon
import dev.fritz2.components.lineUp
import dev.fritz2.components.stackUp
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.params.BackgroundRepeats
import dev.fritz2.styling.params.styled


fun RenderContext.sentFrom(member: String) {
    icon({
        size { normal }
        margins { top { tiny } }
    }) { fromTheme { user } }
    (::span.styled {
        fontWeight { semiBold }
    }) {
        +member
    }
}

fun RenderContext.sentAt(time: String) {
    (::span.styled {
        color { darkerGray }
    }) {
        icon({
            size { small }
            margins { right { tiny } }
        }) { fromTheme { clock } }
        +time
    }
}

fun RenderContext.chatMessage(msg: ChatMessage, self: Boolean) {
    lineUp({
        margins { bottom { tiny } }
        textAlign { if (self) right else left }
        fontSize { smaller }
        alignItems { flexEnd }
    }) {
        reversed(self)
        spacing { smaller }
        items {
            sentFrom(msg.member)
            sentAt(msg.created)
        }
    }
    (::div.styled {
        boxShadow { flat }
        background { color { if (self) primary else secondary } }
        color { "white" }
        padding { small }
        radius { normal }
    }) {
        +msg.content
    }
}

fun RenderContext.chatPage() {
    stackUp({
        alignItems { stretch }
        height { full }
        background {
            image { "/img/y-so-serious-white.png" }
            repeat { repeat }
        }
        padding { normal }
    }) {
        items {
            ChatStore.sub(L.Chat.messages).data.renderEach { msg ->
                val self = msg.member == ChatStore.current.member

                (::div.styled {
                    flex { alignSelf { if (self) flexEnd else flexStart } }
                }) {
                    chatMessage(msg, self)
                }
            }
        }
    }
}
