package app.shared

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
    fun readyToJoin() = room.isNotBlank() && member.isNotBlank()
}

// returning current time in "HH:mm" format
fun currentAsString(): String =
    Clock.System.now().toString().substringAfter('T').dropLast(8)

//        override val validator = object : ComponentValidator<String, Unit>() {
//            override fun validate(data: String, metadata: Unit): List<ComponentValidationMessage> {
//                val name = inspect(data)
//                return when {
//                    name.data.isBlank() ->
//                        listOf(errorMessage(name.id, "Please enter your name."))
//                    name.data.trim().length > 25 ->
//                        listOf(errorMessage(name.id, "Please use a shorter name."))
//                    name.data.trim().length <= 3 ->
//                        listOf(errorMessage(name.id, "Please use a longer name."))
//                    else -> emptyList()
//                }
//            }
//        }

