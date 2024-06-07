package org.lnwza007.relay.service.nip01

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.FiltersX
import org.lnwza007.relay.policy.EventValidateField
import org.lnwza007.relay.policy.FiltersXValidateField
import org.lnwza007.relay.service.nip01.Transform.toEvent
import org.lnwza007.relay.service.nip01.Transform.toFiltersX
import org.lnwza007.relay.service.nip01.Transform.validateJsonElement
import org.lnwza007.util.ShiftTo.toJsonElementMap

@Serializable
sealed class Command

@Serializable
data class EVENT(val event: Event) : Command()

@Serializable
data class REQ(val subscriptionId: String, val filtersX: List<FiltersX>?) : Command()

@Serializable
data class CLOSE(val subscriptionId: String) : Command()

fun parseCommand(payload: String): Pair<Command?, Pair<Boolean, String>> {
    val jsonElement = try {
        Json.parseToJsonElement(payload)
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
            val eventJson: JsonObject = jsonElement[1].jsonObject
            val event: Event = eventJson.toEvent()

            val data: Map<String, JsonElement> = try {
                val obj = jsonElement[1].jsonObject
                obj.toMap()
            } catch (e: Exception) {
                throw IllegalArgumentException("Invalid object at index 1")
            }

            val (status, warning) = validateJsonElement(data, EventValidateField.entries.toTypedArray())

            Pair(
                EVENT(event),
                Pair(status, warning)
            )
        }

        "REQ" -> {
            if (jsonElement.size < 3 || jsonElement[1] !is JsonPrimitive || jsonElement.drop(2).any { it !is JsonObject }) {
                throw IllegalArgumentException("Invalid REQ command format")
            }
            val subscriptionId = jsonElement[1].jsonPrimitive.content
            val filtersJson: List<JsonObject> = jsonElement.drop(2).map { it.jsonObject }

            // ใช้ queue algorithm เพื่อรวบรวมข้อมูลจาก filtersJson ลงใน Map
            val data: MutableMap<String, JsonElement> = mutableMapOf()
            val queue: ArrayDeque<JsonObject> = ArrayDeque(filtersJson)

            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                current.forEach { (key, value) ->
                    data[key] = value
                }
            }

            val filtersX: List<FiltersX>? = try {
                filtersJson.map { it.toFiltersX() }
            } catch (e: Exception) {
                null
            }

            val (status, warning) = validateJsonElement(data, FiltersXValidateField.entries.toTypedArray())

            Pair(
                REQ(subscriptionId, filtersX),
                Pair(status, warning)
            )
        }

        "CLOSE" -> {
            if (jsonElement.size != 2 || jsonElement[1] !is JsonPrimitive) {
                throw IllegalArgumentException("Invalid CLOSE command format")
            }
            val subscriptionId = jsonElement[1].jsonPrimitive.content
            CLOSE(subscriptionId)

            Pair(
                CLOSE(subscriptionId),
                Pair(false, "")
            )

        }

        else -> throw IllegalArgumentException("Unknown command: $type")
    }
}

fun main() {

}