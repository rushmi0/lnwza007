package org.lnwza007.relay.service.nip01

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.FiltersX

@Serializable
sealed class Command

@Serializable
data class EVENT(val event: Event) : Command()

@Serializable
data class REQ(val subscriptionId: String, val filtersX: List<FiltersX>) : Command()

@Serializable
data class CLOSE(val subscriptionId: String) : Command()

fun parseCommand(inputString: String): Command {
    val jsonElement = try {
        Json.parseToJsonElement(inputString)
    } catch (e: Exception) {
        throw IllegalArgumentException("Invalid JSON format")
    }

    if (jsonElement !is JsonArray || jsonElement.size == 0) {
        throw IllegalArgumentException("Invalid command format")
    }

    return when (val type = jsonElement[0].jsonPrimitive.content) {
        "EVENT" -> {
            if (jsonElement.size != 2 || jsonElement[1] !is JsonObject) {
                throw IllegalArgumentException("Invalid EVENT command format")
            }
            val eventJson = jsonElement[1].jsonObject
            val event = Json.decodeFromJsonElement<Event>(eventJson)
            EVENT(event)
        }

        "REQ" -> {
            if (jsonElement.size < 3 || jsonElement[1] !is JsonPrimitive || jsonElement.drop(2).any { it !is JsonObject }) {
                throw IllegalArgumentException("Invalid REQ command format")
            }
            val subscriptionId = jsonElement[1].jsonPrimitive.content
            val filtersJson = jsonElement.drop(2).map { it.jsonObject }
            val filtersX = filtersJson.map { Json.decodeFromJsonElement<FiltersX>(it) }
            REQ(subscriptionId, filtersX)
        }

        "CLOSE" -> {
            if (jsonElement.size != 2 || jsonElement[1] !is JsonPrimitive) {
                throw IllegalArgumentException("Invalid CLOSE command format")
            }
            val subscriptionId = jsonElement[1].jsonPrimitive.content
            CLOSE(subscriptionId)
        }

        else -> throw IllegalArgumentException("Unknown command: $type")
    }
}

