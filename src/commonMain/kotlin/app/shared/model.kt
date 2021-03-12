package app.shared

import dev.fritz2.lenses.Lenses
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

@Lenses
@Serializable
data class ChatMessage(
    val content: String,
    val member: String,
    val type: MessageType = MessageType.MESSAGE,
    @Serializable(with = InstantSerializer::class)
    val created: Instant = Clock.System.now()
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

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}

@Lenses
data class Chat(
    val room: String = "",
    val member: String = "",
    val members: List<String> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val inRoom: Boolean = false
) {
    fun readyToJoin() = room.isNotBlank() && member.isNotBlank()
}

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

