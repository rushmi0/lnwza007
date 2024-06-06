package org.lnwza007

import kotlinx.serialization.*
import kotlinx.serialization.json.*

fun main() {
    val input = """["REQ", "sub_id_123", "filter1", "filter2"]"""
    val json = Json { ignoreUnknownKeys = true } // กำหนดให้ JSON parser ไม่เอา field ที่ไม่รู้จัก

    val parsedArray = json.decodeFromString<List<Any>>(input)

    when (parsedArray.getOrNull(0) as? String) {
        "EVENT" -> {
            val eventJson = parsedArray.getOrNull(1)
            // เผยแพร่เหตุการณ์
            if (eventJson != null && eventJson is JsonElement) {
                println("Publishing event: $eventJson")
                // เพิ่มโค้ดที่ต้องการให้ทำงานกับ eventJson ตรงนี้
            } else {
                println("Invalid event JSON format")
            }
        }
        "REQ" -> {
            val subscriptionId = parsedArray.getOrNull(1) as? String
            val filters = parsedArray.drop(2).mapNotNull { it as? String }
            // สมัครสมาชิกเพื่อรับการอัปเดตใหม่
            if (subscriptionId != null) {
                println("Requesting updates for subscription ID: $subscriptionId with filters: $filters")
                // เพิ่มโค้ดที่ต้องการให้ทำงานกับ subscriptionId และ filters ตรงนี้
            } else {
                println("Invalid REQ format")
            }
        }
        "CLOSE" -> {
            val subscriptionId = parsedArray.getOrNull(1) as? String
            // หยุดการสมัครสมาชิกก่อนหน้านี้
            if (subscriptionId != null) {
                println("Closing subscription ID: $subscriptionId")
                // เพิ่มโค้ดที่ต้องการให้ทำงานกับ subscriptionId ตรงนี้
            } else {
                println("Invalid CLOSE format")
            }
        }
        else -> {
            println("Unknown operation")
        }
    }
}
