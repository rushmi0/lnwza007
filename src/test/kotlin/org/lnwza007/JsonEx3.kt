package org.lnwza007

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

fun main() {
    val inputStrings = listOf(
        """["EVENT", {"event": "some_event", "data": {"key": "value"}}]""",
        """["REQ", "subscription_id_123", {"filter1": "value1"}, {"filter2": "value2"}]""",
        """["CLOSE", "subscription_id_123"]"""
    )

    for (inputString in inputStrings) {
        val inputList = Json.decodeFromString<List<Any>>(inputString)
        val command = inputList[0] as String

        when (command) {
            "EVENT" -> {
                val eventJson = inputList[1] as JsonObject
                handleEvent(eventJson)
            }
            "REQ" -> {
                val subscriptionId = inputList[1] as String
                val filters = inputList.subList(2, inputList.size).map { it as JsonObject }
                handleRequest(subscriptionId, filters)
            }
            "CLOSE" -> {
                val subscriptionId = inputList[1] as String
                handleClose(subscriptionId)
            }
            else -> {
                println("Unknown command: $command")
            }
        }
    }
}

fun handleEvent(eventJson: JsonObject) {
    // จัดการกับเหตุการณ์ที่ได้รับ
    println("Handling event: $eventJson")
}

fun handleRequest(subscriptionId: String, filters: List<JsonObject>) {
    // ดำเนินการตามคำขอและสร้างการสมัครสมาชิกใหม่
    println("Handling request for subscription ID: $subscriptionId with filters: $filters")
}

fun handleClose(subscriptionId: String) {
    // ยกเลิกการสมัครสมาชิก
    println("Handling close request for subscription ID: $subscriptionId")
}
