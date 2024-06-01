package org.lnwza007.relay.service.nip01

import jakarta.inject.Singleton
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.*
import org.lnwza007.relay.service.nip01.Transform.toEvent
import org.lnwza007.relay.service.nip01.Transform.toFiltersX
import org.lnwza007.relay.service.nip01.Transform.validateDataType
import org.lnwza007.util.ShiftTo.toJsonElementMap
import org.slf4j.LoggerFactory

@Singleton
open class VerificationFactory {


    /**
     * ฟังก์ชัน checkFieldNames ใช้ในการตรวจสอบชื่อฟิลด์ข้อมูลว่าตรงกับนโยบายหรือไม่
     * @param receive ข้อมูลที่ต้องการตรวจสอบ
     * @param relayPolicy นโยบายการตรวจสอบฟิลด์
     * @return ผลลัพธ์จริงหรือเท็จว่าชื่อฟิลด์ตรงกับนโยบายหรือไม่
     */
    private fun checkFieldNames(
        receive: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>
    ): Pair<Boolean, String?> {

        // สร้างเซ็ตของชื่อฟิลด์ที่อนุญาต
        val allowedFields: Set<String> = relayPolicy.map { it.fieldName }.toSet()

        // ตรวจสอบหาฟิลด์ที่ไม่อนุญาต
        val invalidFields: List<String> = receive.keys.filter { it !in allowedFields }

        // ถ้าไม่มีฟิลด์ที่ไม่อนุญาต คืนค่าคู่ (true, null) ถ้ามีคืนค่าคู่ (false, รายชื่อฟิลด์ที่ไม่อนุญาต)
        return if (invalidFields.isEmpty()) Pair(true, null) else Pair(
            false,
            invalidFields.joinToString(", ")
        )
    }


    /**
     * ฟังก์ชัน mapToObject ใช้ในการแปลงข้อมูล JSON เป็นออบเจ็กต์ตามนโยบายที่กำหนด
     * @param map ข้อมูล JSON ที่ต้องการแปลงไปเป็น Object
     * @param relayPolicy นโยบายการตรวจสอบฟิลด์
     * @param converter ฟังก์ชันที่ใช้ในการแปลงข้อมูล
     * @return คือ Array ที่มีสองสมาชิกแทนที่จะส่งค่ากลับเป็นคู่ของ `String?` และ `T?`
     *         โดยสมาชิกแรกเป็น `String?` และสมาชิกที่สองเป็น `T?`
     */
    fun <T> mapToObject(
        map: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>,
        converter: (Map<String, JsonElement>) -> T
    ): Triple<Boolean, String?, T?> {
        //LOG.info("map: $map")
        // ตรวจสอบชื่อฟิลด์ว่าตรงกับนโยบายหรือไม่
        val (isFieldNamesValid, fieldNamesError) = checkFieldNames(map, relayPolicy)

        // แจ้งเตือนเมื่อมีฟิลด์ที่ไม่ตรงกับนโยบาย
        if (!isFieldNamesValid) {
            val message = "Invalid fields: $fieldNamesError"
            LOG.warn(message)
            return Triple(false, message, null)
        }

        return Triple(true, "valid", converter(map))
    }


    /////////////////////////////////////////////////////////////////////////////


    private fun inspectDataType(
        receive: JsonElement
    ): Any {
        return when (receive) {
            is JsonPrimitive -> {
                when {
                    receive.isString -> receive.content::class.java
                    receive.booleanOrNull != null -> receive.boolean::class.java
                    receive.longOrNull != null -> receive.long::class.java
                    receive.doubleOrNull != null -> receive.double::class.java
                    else -> receive.content::class.java
                }
            }

            is JsonArray -> receive.map { inspectDataType(it) }::class.java
            is JsonObject -> receive.toMap().mapValues { inspectDataType(it.value) }::class.java
            else -> receive.toString()::class.java
        }
    }

    private fun inspectValue(
        receive: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>
    ): Pair<Boolean, String?> {
        return when {
            relayPolicy.isArrayOf<FiltersXValidateField>() -> {
                println("\nFiltersX Validate Field")
                val (i, filtersX) = receive.toFiltersX()

                Pair(true, "$i Data: $filtersX")
            }

            relayPolicy.isArrayOf<EventValidateField>() -> {
                println("\nEvent Validate Field")
                val (i, filtersX) = receive.toEvent()
                Pair(true, "$i Data: $filtersX")
            }

            else -> Pair(false, "Unsupported field type")
        }
    }

    private inline fun <reified T> Array<*>.isArrayOf(): Boolean = all { it is T }


    fun validateDataType(
        receive: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>
    ): Pair<Boolean, String?> {

        receive.forEach { (fieldName, fieldValue) ->
            val expectedType = relayPolicy.find { policy -> policy.fieldName == fieldName }?.fieldType
            val actualType = inspectDataType(fieldValue)

            //println("fieldName: $fieldName, fieldValue: $fieldValue, actualType: $actualType")
            val status = expectedType != actualType

            if (status) {
                return Pair(false, "Invalid data type at [$fieldName] field")
            }
        }

        return inspectValue(receive, relayPolicy)
    }

    private val LOG = LoggerFactory.getLogger(VerificationFactory::class.java)

}

fun main() {


    val invalidData = """
          {
            "kind":0,
            "created_at":"1716448321",
            "tags":[["alt","User profile for lnwza007"]],
            "content":"{\"name\":\"lnwza007\",\"gender\":\"\",\"area\":\"\",\"picture\":\"https://image.nostr.build/552b5424ebd3c66be6f588e08c2f427e04423f11e80514414215b5ae00877b28.gif\",\"lud16\":\"rushmi0@getalby.com\",\"website\":\"https://github.com/rushmi0\",\"display_name\":\"lnwza007\",\"banner\":\"\",\"about\":\"แดดกรุงเทพที่ร้อนจ้า ยังแพ้ตัวข้าที่ร้อน sat\"}",
            "pubkey":"e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3",
            "id":"ecfdf5d329ae69bdca40f04a33a8f8447b83824f958a8db926430cd8b2aeb350",
            "sig":"6a7898997ceb936fb6f660848baf8185f84ab22ff45aa3fc36eabad577bb4fae739bfdcd3d428d52146c6feaf9264bbc8f82121ddb8eeb85ce242ff79a1b0948"
          }
    """.trimIndent()

    val validData = """
          {
            "id": "0000005b0fc51e70b66db99ba1708b1a1b008c30db35d19d35146b3e09756029",
            "pubkey": "161498ed3277aa583c301288de5aafda4f317d2bf1ad0a880198a9dede37a6aa",
            "created_at": 1716617176,
            "kind": 1,
            "tags": [
              ["nonce","19735841","23"]
            ],
            "content": "My custom content",
            "sig": "954c662c9ee29ccad8a1f30d22b9a5cefcea774f72428ec7344b65e4f31fff24fc4dd0b7874a4d10a1a4c012de013df19a7c33018dda5f1207280f9a28193498"
          }
    """.trimIndent()

    val jsonElemen1 = invalidData.toJsonElementMap()
    val jsonElemen2 = validData.toJsonElementMap()

    println(
        validateDataType(
            jsonElemen1,
            EventValidateField.entries.toTypedArray()
        )
    )
    println(validateDataType(jsonElemen2, EventValidateField.entries.toTypedArray()))

    // ตัวอย่างที่ 2: FiltersXValidateField
    val jsonREQ1 = """
        {
            "authors": [
              "e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3"
            ],
            "kinds": [4],
            "since": 1715181359
        }
    """.trimIndent()

    val jsonElemen3 = jsonREQ1.toJsonElementMap()
    println(validateDataType(jsonElemen3, FiltersXValidateField.entries.toTypedArray()))

}