package app.frontend.views

import app.frontend.ChatStore
import app.shared.ChatMessage
import app.shared.L
import dev.fritz2.components.icon
import dev.fritz2.components.lineUp
import dev.fritz2.components.stackUp
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.styling.div
import dev.fritz2.styling.span


fun RenderContext.sentFrom(member: String) {
    icon({
        size { normal }
        margins { top { tiny } }
    }) { fromTheme { user } }
    span({
        fontWeight { semiBold }
    }) {
        +member
    }
}

fun RenderContext.sentAt(time: String) {
    span({
        color { gray500 }
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
    div({
        boxShadow { flat }
        background { color { if (self) primary.main else secondary.main } }
        color { "white" }
        padding { small }
        radius { normal }
    }) {
        +msg.content
    }
}

fun RenderContext.chatPage() {
    div({
        minHeight { full }
        width { full }
        background {
            image { "/img/y-so-serious-white.png" }
            repeat { repeat }
        }
        padding { normal }
    }) {
        stackUp({
            alignItems { stretch }
        }) {
            items {
                ChatStore.sub(L.Chat.messages).data.renderEach { msg ->
                    val self = msg.member == ChatStore.current.member

                    div({
                        flex { alignSelf { if (self) flexEnd else flexStart } }
                    }) {
                        chatMessage(msg, self)
                    }
                }
            }
        }
    }

}
