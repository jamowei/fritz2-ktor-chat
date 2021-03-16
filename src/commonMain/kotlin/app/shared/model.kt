package app.shared

import dev.fritz2.components.validation.ComponentValidationMessage
import dev.fritz2.components.validation.ComponentValidator
import dev.fritz2.components.validation.errorMessage
import dev.fritz2.identification.inspect
import dev.fritz2.lenses.Lenses
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Lenses
@Serializable
data class ChatMessage(
    val content: String,
    val member: String,
    val type: MessageType = MessageType.MESSAGE,
    val created: String = currentAsString()
) {
    companion object {
        fun fromJson(source: String): ChatMessage =
            Json.decodeFromString(serializer(), source)
    }

    fun toJson(): String =
        Json.encodeToString(serializer(), this)
}

enum class MessageType {
    MESSAGE, JOINING, LEAVING
}

@Lenses
data class Chat(
    val room: String = "",
    val member: String = "",
    val members: List<String> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val joined: Boolean = false
) {
    val readyToJoin
        get() = room.isNotBlank() && member.isNotBlank()
}

// returning current time in "HH:mm" format
fun currentAsString(): String =
    Clock.System.now().toString().substringAfter('T').dropLast(8)

object ChatValidator : ComponentValidator<Chat, Unit>() {
    @ExperimentalStdlibApi
    override fun validate(data: Chat, metadata: Unit): List<ComponentValidationMessage> {
        val member = inspect(data).sub(L.Chat.member)
        val room = inspect(data).sub(L.Chat.room)

        return buildList {
            if (member.data.isBlank()) add(errorMessage(member.id, "Sorry, you have to enter a name"))
            else if (member.data.trim().length > 25) add(errorMessage(member.id, "Please use a shorter name."))
            if (room.data.isBlank()) add(errorMessage(room.id, "You have to enter the title of your chat"))
        }
    }
}

