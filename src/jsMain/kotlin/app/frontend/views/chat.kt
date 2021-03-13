package app.frontend.views

import app.frontend.ChatStore
import app.shared.ChatMessage
import app.shared.L
import dev.fritz2.components.icon
import dev.fritz2.components.lineUp
import dev.fritz2.components.stackUp
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.params.styled
import kotlinx.coroutines.ExperimentalCoroutinesApi


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
        color { gray }
    }) {
        icon({
            size { small }
            margins { right { tiny } }
        }) { fromTheme { clock } }
        +time
    }
}

@ExperimentalCoroutinesApi
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
            sentAt(msg.created.print())
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

@ExperimentalCoroutinesApi
fun RenderContext.chatPage() {
    stackUp({
        alignItems { stretch }
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
