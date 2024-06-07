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

        else -> throw IllegalArgumentException("Unknown command type: $type")
    }
}

fun main() {

    val inputStrings = listOf(
        """
            [
                "EVENT", 
                {
                  "id": "000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358",
                  "pubkey": "a48380f4cfcc1ad5378294fcac36439770f9c878dd880ffa94bb74ea54a6f243",
                  "created_at": 1651794653,
                  "kind": 1,
                  "tags": [
                    ["nonce", "776797", "20"]
                  ],
                  "content": "It's just me mining my own business",
                  "sig": "284622fc0a3f4f1303455d5175f7ba962a3300d136085b9566801bc2e0699de0c7e31e44c81fb40ad9049173742e904713c3594a1da0fc5d2382a25c11aba977"
                  }
            ]""",

        """
            [
                "REQ", 
                "ffff", 
                {"search": "purple", "kinds": [1], "since": 1715181359}, 
                {"kinds": [1], "authors": ["161498ed3277aa583c301288de5aafda4f317d2bf1ad0a880198a9dede37a6aa"]}
            ] 
        """.trimIndent(),

        """["CLOSE", "ffff"]"""
    )

    inputStrings.forEach { inputString ->
        try {
            when (val command = parseCommand(inputString)) {
                is EVENT -> println("Handling event: ${command.event}")
                is REQ -> println("Handling request for subscription ID: ${command.subscriptionId} with filters \n1: ${command.filtersX[0]} \n2: ${command.filtersX[1]}")
                is CLOSE -> println("Handling close request for subscription ID: ${command.subscriptionId}")
            }
        } catch (e: Exception) {
            println("Failed to handle command: ${e.message}")
        }
    }
}

