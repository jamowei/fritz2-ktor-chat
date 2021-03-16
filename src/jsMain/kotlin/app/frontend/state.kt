package app.frontend

import app.shared.Chat
import app.shared.ChatMessage
import app.shared.ChatValidator
import app.shared.MessageType
import dev.fritz2.binding.RootStore
import dev.fritz2.components.alert
import dev.fritz2.components.showToast
import dev.fritz2.components.validation.ComponentValidator
import dev.fritz2.components.validation.WithValidator
import dev.fritz2.remote.*
import dev.fritz2.routing.encodeURIComponent
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

object ChatStore : RootStore<Chat>(Chat(router.current["room"].orEmpty(), router.current["member"].orEmpty())), WithValidator<Chat, Unit> {
    private lateinit var membersService: Request
    private lateinit var session: Session

    override val validator: ComponentValidator<Chat, Unit> = ChatValidator

    private suspend fun loadUsers(member: String): List<String> =
        Json.decodeFromString(ListSerializer(String.serializer()), membersService.get().getBody()) - member

    val join = handle { chat ->
        if (validator.isValid(chat, Unit)) {
            membersService = http("/members/${chat.room}")
            session = websocket("ws://${window.location.host}/chat/${chat.room}").connect().also {
                it.messages.body.map { msg -> ChatMessage.fromJson(msg) } handledBy receive
                it.send(ChatMessage("", chat.member, MessageType.JOINING).toJson())
            }
            delay(200)
            syncBy(scrollDown)
            Chat(chat.room, chat.member, loadUsers(chat.member), emptyList(), true)
        } else chat
    }

    private val receive = handle<ChatMessage> { chat, msg ->
        chat.copy(
            members = if (msg.type == MessageType.JOINING || msg.type == MessageType.LEAVING) loadUsers(chat.member) else chat.members,
            messages = chat.messages + msg
        )
    }

    val send = handle<String> { chat, msg ->
        if (msg.isNotBlank()) {
            chat.copy(messages = chat.messages + ChatMessage(msg, chat.member).also { session.send(it.toJson()) })
        }
        else chat
    }

    private val scrollDown = handle { msgs ->
        document.getElementById("chat-messages")?.let {
            it.scrollTo(0.0, it.scrollHeight.toDouble())
        }
        msgs
    }

    val invite = handle {
        copyToClipboard(
            "${window.location.protocol}//${window.location.host}/#room=${encodeURIComponent(it.room)}"
        )
        showToast {
            content {
                alert {
                    content("Invitation-URL copied to your clipboard")
                }
            }
        }
        it
    }

    val joined = data.map { it.joined }.distinctUntilChanged()

    init {
        if (current.readyToJoin) join()
    }
}

