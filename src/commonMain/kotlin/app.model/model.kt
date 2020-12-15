package app.model

import dev.fritz2.lenses.Lenses
import dev.fritz2.resource.Resource
import dev.fritz2.resource.ResourceSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
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
        val resourceSerializer = object : ResourceSerializer<ChatMessage> {
            override fun read(source: String): ChatMessage = Json.decodeFromString(serializer(), source)
            override fun readList(source: String): List<ChatMessage> =
                Json.decodeFromString(ListSerializer(serializer()), source)

            override fun write(item: ChatMessage): String = Json.encodeToString(serializer(), item)
            override fun writeList(items: List<ChatMessage>): String =
                Json.encodeToString(ListSerializer(serializer()), items)
        }
        val resource = Resource(
            { "${it.member}_${it.created.epochSeconds}" },
            resourceSerializer,
            ChatMessage("", "")
        )
    }
}

enum class MessageType {
    MESSAGE, JOINING, LEAVING
}

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}