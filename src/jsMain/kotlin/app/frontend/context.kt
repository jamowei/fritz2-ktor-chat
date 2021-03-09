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
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class ChatStore(val room: String, val name: String) : RootStore<Chat>(Chat()) {
    private val membersService = http("/members/$room")
    private val session = websocket("ws://${window.location.host}/chat/$room").connect()

    private suspend fun loadUsers(): List<String> =
        Json.decodeFromString(ListSerializer(String.serializer()), membersService.get().getBody()) - name

    val enter = handle {
        console.log("enter")
        session.send(ChatMessage("", name, MessageType.JOINING).toJson())
        delay(200)
        Chat(loadUsers())
    }

    val receive = handle<ChatMessage> { chat, msg ->
        console.log("receive $msg")
        Chat(
            if (msg.type == MessageType.JOINING || msg.type == MessageType.LEAVING) loadUsers() else chat.members,
            chat.messages + msg
        )
    }

    val send = handle<ChatMessage> { chat, msg ->
        console.log("send $msg")
        session.send(msg.toJson())
        val msgs = chat.messages + msg
        console.log("+++ $msgs")
        chat.copy(messages = msgs)
    }

    val scrollDown = handle { msgs ->
        console.log("scroll")
        document.getElementById("chat-messages")?.let {
            it.scrollTo(0.0, it.scrollHeight.toDouble())
        }
        msgs
    }

    val invite = handle {
        copyToClipboard(
            "${window.location.protocol}//${window.location.host}/#room=$room"
        )
        it
    }

    val inRoom = room.isNotBlank() && name.isNotBlank()

    init {
        console.log("init")
        if (inRoom) {
            enter()
            session.messages.body.map { ChatMessage.fromJson(it) } handledBy receive
            syncBy(scrollDown)
        }
    }
}

val appContext = router.data.map {
    ChatStore(it["room"].orEmpty(), it["member"].orEmpty() )
}.shareIn(MainScope(), SharingStarted.Lazily)

