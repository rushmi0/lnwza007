package org.lnwza007.relay.service.nip01.command

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.FiltersX
import org.lnwza007.relay.policy.EventValidateField
import org.lnwza007.relay.policy.FiltersXValidateField
import org.lnwza007.relay.service.nip01.Transform.convertToFiltersXObject
import org.lnwza007.relay.service.nip01.Transform.toEvent
import org.lnwza007.relay.service.nip01.Transform.toFiltersX
import org.lnwza007.relay.service.nip01.Transform.validateJsonElement

@Serializable
sealed class Command

@Serializable
data class EVENT(val event: Event) : Command()

@Serializable
data class REQ(val subscriptionId: String, val filtersX: List<FiltersX>?) : Command()

@Serializable
data class CLOSE(val subscriptionId: String) : Command()

@Serializable
data class AUTH(val challenge: String) : Command()

object DetectCommand {

    fun parseCommand(payload: String): Pair<Command?, Pair<Boolean, String>> {
        val json = Json { ignoreUnknownKeys = true }
        val jsonElement = try {
            json.parseToJsonElement(payload)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid: JSON format")
        }

        if (jsonElement !is JsonArray || jsonElement.isEmpty()) {
            throw IllegalArgumentException("Invalid: command format")
        }

        return when (val type = jsonElement[0].jsonPrimitive.content) {
            "EVENT" -> parseEventCommand(jsonElement)
            "REQ" -> parseReqCommand(jsonElement)
            "CLOSE" -> parseCloseCommand(jsonElement)
            else -> throw IllegalArgumentException("Unknown command: $type")
        }
    }

    private fun parseEventCommand(jsonArray: JsonArray): Pair<Command, Pair<Boolean, String>> {
        if (jsonArray.size != 2 || jsonArray[1] !is JsonObject) {
            throw IllegalArgumentException("Invalid EVENT command format")
        }
        val eventJson = jsonArray[1].jsonObject
        val event: Event = eventJson.toEvent()
        val data: Map<String, JsonElement> = eventJson.toMap()

        val (status, warning) = validateJsonElement(data, EventValidateField.entries.toTypedArray())
        return EVENT(event) to (status to warning)
    }

    private fun parseReqCommand(jsonArray: JsonArray): Pair<Command, Pair<Boolean, String>> {
        if (jsonArray.size < 3 || jsonArray[1] !is JsonPrimitive || jsonArray.drop(2).any { it !is JsonObject }) {
            throw IllegalArgumentException("Invalid REQ command format")
        }
        val subscriptionId: String = jsonArray[1].jsonPrimitive.content
        val filtersJson: List<JsonObject> = jsonArray.drop(2).map { it.jsonObject }

        val data: Map<String, JsonElement> = filtersJson.flatMap { it.entries }.associate { it.key to it.value }

        val filtersX: List<FiltersX>? = try {
            filtersJson.map { convertToFiltersXObject(it) }
        } catch (e: Exception) {
            null
        }

        val (status, warning) = validateJsonElement(data, FiltersXValidateField.entries.toTypedArray())
        return REQ(subscriptionId, filtersX) to (status to warning)
    }
    private fun parseCloseCommand(jsonArray: JsonArray): Pair<Command, Pair<Boolean, String>> {
        if (jsonArray.size != 2 || jsonArray[1] !is JsonPrimitive) {
            throw IllegalArgumentException("Invalid CLOSE command format")
        }
        val subscriptionId = jsonArray[1].jsonPrimitive.content
        return CLOSE(subscriptionId) to (true to "")
    }

}