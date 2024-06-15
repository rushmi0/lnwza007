package org.lnwza007

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import org.lnwza007.relay.service.nip01.Transform.toJsonString


@Serializable(with = NostrResponseSerializer::class)
sealed class NostrResponse {

    @Serializable
    @SerialName("EVENT")
    data class Event(val subscriptionId: String, val event: Event?) : NostrResponse()

    @Serializable
    @SerialName("OK")
    data class Ok(val eventId: String, val status: Boolean, val message: String = "") : NostrResponse()

    @Serializable
    @SerialName("EOSE")
    data class Eose(val subscriptionId: String) : NostrResponse()

    @Serializable
    @SerialName("CLOSED")
    data class Closed(val subscriptionId: String, val message: String) : NostrResponse()

    @Serializable
    @SerialName("NOTICE")
    data class Notice(val message: String) : NostrResponse()
}

object NostrResponseSerializer : KSerializer<NostrResponse> {

    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("NostrResponse", PolymorphicKind.SEALED) {
        element("EVENT", NostrResponse.Event.serializer().descriptor)
        element("OK", NostrResponse.Ok.serializer().descriptor)
        element("EOSE", NostrResponse.Eose.serializer().descriptor)
        element("CLOSED", NostrResponse.Closed.serializer().descriptor)
        element("NOTICE", NostrResponse.Notice.serializer().descriptor)
    }

    override fun deserialize(decoder: Decoder): NostrResponse {
        require(decoder is JsonDecoder)
        val element = decoder.decodeJsonElement().jsonArray
        val type = element[0].jsonPrimitive.content
        return when (type) {
            "EVENT" -> decoder.json.decodeFromJsonElement(NostrResponse.Event.serializer(), element)
            "OK" -> decoder.json.decodeFromJsonElement(NostrResponse.Ok.serializer(), element)
            "EOSE" -> decoder.json.decodeFromJsonElement(NostrResponse.Eose.serializer(), element)
            "CLOSED" -> decoder.json.decodeFromJsonElement(NostrResponse.Closed.serializer(), element)
            "NOTICE" -> decoder.json.decodeFromJsonElement(NostrResponse.Notice.serializer(), element)
            else -> throw SerializationException("Unknown type: $type")
        }
    }

    override fun serialize(encoder: Encoder, value: NostrResponse) {
        require(encoder is JsonEncoder)
        val jsonString = when (value) {
            is NostrResponse.Event -> value.toJsonString()
            is NostrResponse.Ok -> value.toJsonString()
            is NostrResponse.Eose -> value.toJsonString()
            is NostrResponse.Closed -> value.toJsonString()
            is NostrResponse.Notice -> value.toJsonString()
        }
        encoder.encodeString(jsonString)
    }
}

// Example usage
fun main() {
    val responses = listOf(
        NostrResponse.Ok("b1a649ebe8...", true),
        NostrResponse.Ok("b1a649ebe8...", false, "duplicate: have this event"),
        NostrResponse.Ok("b1a649ebe8...", false, "invalid: missing fields"),
        NostrResponse.Eose("b1a649ebe8..."),
        NostrResponse.Notice("unrecognised filter item")
    )

    responses.forEach {
        println(it.toJsonString())
    }

    println(NostrResponse.Ok("b1a649ebe8...", true).javaClass)

    val listString = listOf("OK", "b1a649ebe8...", true, "")
    val result: String = listString.toJsonString()
    println(result) // ["OK","b1a649ebe8...",true,""]
}
