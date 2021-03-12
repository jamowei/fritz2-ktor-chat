package app.frontend

import app.shared.Chat
import app.shared.ChatMessage
import app.shared.MessageType
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.invoke
import dev.fritz2.remote.body
import dev.fritz2.remote.getBody
import dev.fritz2.remote.http
import dev.fritz2.remote.websocket
import dev.fritz2.routing.router
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

val defaultRoute = mapOf<String, String>()
val router = router(defaultRoute)

object ChatStore : RootStore<Chat>(Chat(router.current["room"].orEmpty(), router.current["member"].orEmpty())) {
    private val membersService by lazy { http("/members/${current.room}") }
    private val session by lazy {
        websocket("ws://${window.location.host}/chat/${current.room}").connect().also {
            it.messages.body.map { msg -> ChatMessage.fromJson(msg) } handledBy receive
        }
    }

    private suspend fun loadUsers(member: String): List<String> =
        Json.decodeFromString(ListSerializer(String.serializer()), membersService.get().getBody()) - member

    val join = handle {
        console.log("join")
        session.send(ChatMessage("", it.member, MessageType.JOINING).toJson())
        syncBy(scrollDown)
        it.copy(members = loadUsers(it.member), messages = emptyList())
    }

    private val receive = handle<ChatMessage> { chat, msg ->
        console.log("receive $msg")
        chat.copy(
            members = if (msg.type == MessageType.JOINING || msg.type == MessageType.LEAVING) loadUsers(chat.member) else chat.members,
            messages = chat.messages + msg
        )
    }

    val send = handle<ChatMessage> { chat, msg ->
        console.log("send $msg")
        session.send(msg.toJson())
        val msgs = chat.messages + msg
        console.log("+++ $msgs")
        chat.copy(messages = msgs)
    }

    private val scrollDown = handle { msgs ->
        console.log("scroll")
        document.getElementById("chat-messages")?.let {
            it.scrollTo(0.0, it.scrollHeight.toDouble())
        }
        msgs
    }

    val invite = handle {
        copyToClipboard(
            "${window.location.protocol}//${window.location.host}/#room=${current.room}"
        )
        it
    }

    init {
        if (current.inRoom()) join()
    }

}

