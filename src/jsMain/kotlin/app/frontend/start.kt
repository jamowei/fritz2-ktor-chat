package app.frontend

import dev.fritz2.dom.html.RenderContext

fun RenderContext.startPage() {

    div("chat") {
        div("chat-header clearfix") {
            div("chat-about") {
                div("chat-with") {
                    +"Create a new chatroom"
                }
                div("chat-num-messages") {
                    +"Fill in user and room name"
                }
            }
        }
    }

}