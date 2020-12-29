package app.model

import dev.fritz2.lenses.IdProvider
import dev.fritz2.lenses.Lenses
import dev.fritz2.resource.Resource
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
)

object ChatResource : Resource<ChatMessage, String> {
    override val idProvider: IdProvider<ChatMessage, String> = { "${it.member}_${it.created.epochSeconds}" }

    override fun deserialize(source: String): ChatMessage =
        Json.decodeFromString(ChatMessage.serializer(), source)

    override fun serialize(item: ChatMessage): String =
        Json.encodeToString(ChatMessage.serializer(), item)

    override fun deserializeList(source: String): List<ChatMessage> =
        Json.decodeFromString(ListSerializer(ChatMessage.serializer()), source)

    override fun serializeList(items: List<ChatMessage>): String =
        Json.encodeToString(ListSerializer(ChatMessage.serializer()), items)
}

enum class MessageType {
    MESSAGE, JOINING, LEAVING
}

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
}