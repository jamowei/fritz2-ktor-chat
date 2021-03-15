package app.frontend

import app.shared.Chat
import app.shared.ChatMessage
import app.shared.MessageType
import dev.fritz2.binding.RootStore
import dev.fritz2.binding.invoke
import dev.fritz2.components.randomId
import dev.fritz2.remote.*
import dev.fritz2.routing.router
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

val router = router(emptyMap())

object ChatStore : RootStore<Chat>(Chat(router.current["room"].orEmpty(), router.current["member"].orEmpty())) {
    private lateinit var membersService: Request
    private lateinit var session: Session

    private suspend fun loadUsers(member: String): List<String> =
        Json.decodeFromString(ListSerializer(String.serializer()), membersService.get().getBody()) - member

    val join = handle { chat ->
        val room = chat.room.ifBlank { randomId() }
        val member = chat.member.ifBlank { "someOne ${randomId()}" }
        console.log("joining room $room with member $member")
        membersService = http("/members/$room")
        session = websocket("ws://${window.location.host}/chat/$room").connect().also {
            it.messages.body.map { msg -> ChatMessage.fromJson(msg) } handledBy receive
            it.send(ChatMessage("", member, MessageType.JOINING).toJson())
        }
        delay(200)
        syncBy(scrollDown)
        Chat(room, member, loadUsers(member), emptyList(), true)
    }

    private val receive = handle<ChatMessage> { chat, msg ->
        console.log("receive $msg")
        chat.copy(
            members = if (msg.type == MessageType.JOINING || msg.type == MessageType.LEAVING) loadUsers(chat.member) else chat.members,
            messages = chat.messages + msg
        )
    }

    val send = handle<String> { chat, msg ->
        chat.copy(messages = chat.messages + ChatMessage(msg, chat.member)
            .also { session.send(it.toJson()) }
        )
    }

    //FIXME: why not in receive
    private val scrollDown = handle { msgs ->
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

    val joined = data.map { it.joined }.distinctUntilChanged()

    init {
        if (current.readyToJoin()) join()
    }
}

