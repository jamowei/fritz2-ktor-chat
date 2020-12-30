package app.frontend

import app.model.ChatMessage
import app.model.MessageType
import dev.fritz2.binding.RootStore
import dev.fritz2.dom.html.Keys
import dev.fritz2.dom.html.RenderContext
import dev.fritz2.dom.key
import dev.fritz2.remote.body
import dev.fritz2.remote.getBody
import dev.fritz2.remote.http
import dev.fritz2.remote.websocket
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import dev.fritz2.binding.invoke

fun RenderContext.chatPage(room: String, name: String) {

    val membersStore = object : RootStore<List<String>>(emptyList()) {
        val remote = http("/members/$room")

        val load = handle {
            Json.decodeFromString(ListSerializer(String.serializer()), remote.get().getBody()) - name
        }

        init { load() }
    }

    val messagesStore = object : RootStore<List<ChatMessage>>(emptyList()) {
        val session = websocket("ws://localhost:8080/chat/$room").connect()

        val receive = handle<ChatMessage> { msgs, msg ->
            when (msg.type) {
                MessageType.JOINING, MessageType.LEAVING -> membersStore.load()
            }
            msgs + msg
        }

        val send = handle<ChatMessage> { msgs, msg ->
            console.info("send: $msg")
            session.send(msg.toJson())
            msgs + msg
        }

        // join chat room
        val join = handle { msgs ->
            console.info("joining room: $name")
            session.send(ChatMessage("", name, MessageType.JOINING).toJson())
            msgs
        }

        init {
            join()
            session.messages.body.map { ChatMessage.fromJson(it) } handledBy receive
        }
    }

    div {
        +"Chat Page"
        ul {
            membersStore.data.renderEach {
                li { +it }
            }
        }
        hr {}
        div {
            messagesStore.data.renderEach { msg ->
                div {
                    p { +"${msg.member}: ${msg.content}" }
                }
            }
        }
        div {
            label { +"Message" }
            input {
                keyups.key()
                    .filter { it.isKey(Keys.Enter) }
                    .map { ChatMessage(domNode.value, name).also { domNode.value = "" } }
                    .handledBy(messagesStore.send)
            }
        }
    }

}