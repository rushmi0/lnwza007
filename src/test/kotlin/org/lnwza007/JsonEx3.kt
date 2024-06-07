package org.lnwza007

//@Serializable
//sealed class Command
//
//@Serializable
//data class Event(val event: String, val data: JsonObject) : Command()
//
//@Serializable
//data class Request(val subscriptionId: String, val filtersX: List<JsonObject>) : Command()
//
//@Serializable
//data class Close(val subscriptionId: String) : Command()

//fun main() {
//
//    val inputStrings = listOf(
//        """["EVENT", {"event": "some_event", "data": {"key": "value"}}]""",
//        """["REQ", "subscription_id_123", [{"filter1": "value1"}, {"filter2": "value2"}]]""",
//        """["CLOSE", "subscription_id_123"]"""
//    )
//
//    inputStrings.forEach { inputString ->
//        val jsonElement = Json.parseToJsonElement(inputString)
//        val command = when (val type = jsonElement.jsonArray[0].jsonPrimitive.content) {
//            "EVENT" -> {
//                val eventJson = jsonElement.jsonArray[1].jsonObject
//                Json.decodeFromJsonElement<EventCommand>(eventJson)
//            }
//            "REQ" -> {
//                val subscriptionId = jsonElement.jsonArray[1].jsonPrimitive.content
//                val filtersJson = jsonElement.jsonArray[2].jsonArray.map { it.jsonObject }
//                Json.decodeFromJsonElement<RequestCommand>(Json.encodeToJsonElement(RequestCommand(subscriptionId, filtersJson)))
//            }
//            "CLOSE" -> {
//                val subscriptionId = jsonElement.jsonArray[1].jsonPrimitive.content
//                CloseCommand(subscriptionId)
//            }
//            else -> throw IllegalArgumentException("Unknown command type: $type")
//        }
//        when (command) {
//            is EventCommand -> println("Handling event: $command")
//            is RequestCommand ->  println("Handling request for subscription ID: ${command.subscriptionId} with filters: ${command.filtersX}")
//            is CloseCommand -> println("Handling close request for subscription ID: ${command.subscriptionId}")
//        }
//    }
//}
