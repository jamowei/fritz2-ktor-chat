package app.frontend

import app.model.ChatMessage
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.RenderContext

fun RenderContext.chatPage(room: String, name: String) {

    val messageStore = object : RootStore<List<ChatMessage>>(emptyList()) {

    }

    div {
        +"Chat Page"
    }

}