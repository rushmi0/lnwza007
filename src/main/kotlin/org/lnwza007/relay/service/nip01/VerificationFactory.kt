package org.lnwza007.relay.service.nip01

import jakarta.inject.Singleton
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.*
import org.lnwza007.util.ShiftTo.toJsonElementMap
import org.slf4j.LoggerFactory

@Singleton
open class VerificationFactory {

    private fun checkFieldNames(
        receive: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>
    ): Pair<Boolean, String?> {
        val allowedFields: Set<String> = relayPolicy.map { it.fieldName }.toSet()
        val invalidFields: List<String> = receive.keys.filter { it !in allowedFields }
        val msgError = "Unsupported [${invalidFields.joinToString(", ")}] field"
        return if (invalidFields.isEmpty()) Pair(true, null) else Pair(false, msgError)
    }

    fun <T> mapToObject(
        receive: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>,
        converter: (Map<String, JsonElement>) -> T
    ): Flow<Triple<Boolean, String?, T?>> = flow {
        val (isFieldNamesValid: Boolean, fieldNamesError: String?) = checkFieldNames(receive, relayPolicy)
        val (isDataValid: Boolean, dataError: String?) = validateDataType(receive, relayPolicy)

        val status: Boolean = isFieldNamesValid && isDataValid

        if (!isFieldNamesValid) {
            emit(Triple(isFieldNamesValid, fieldNamesError, null))
            return@flow
        }

        if (!isDataValid) {
            emit(Triple(isDataValid, dataError, null))
            return@flow
        }

        emit(Triple(status, null, converter(receive)))
    }

    private fun inspectDataType(receive: JsonElement): Any {
        return when (receive) {
            is JsonPrimitive -> when {
                receive.isString -> receive.content::class.java
                receive.booleanOrNull != null -> receive.boolean::class.java
                receive.longOrNull != null -> receive.long::class.java
                receive.doubleOrNull != null -> receive.double::class.java
                else -> receive.content::class.java
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
            relayPolicy.isArrayOf<FiltersXValidateField>() -> Pair(true, "Data: FiltersX")
            relayPolicy.isArrayOf<EventValidateField>() -> Pair(true, "Data: Event")
            else -> Pair(false, "Unsupported field type")
        }
    }

    private inline fun <reified T> Array<*>.isArrayOf(): Boolean = all { it is T }

    /*
    fun validateDataType(
        receive: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>
    ): Pair<Boolean, String?> {
        receive.forEach { (fieldName, fieldValue) ->
            val expectedType = relayPolicy.find { policy -> policy.fieldName == fieldName }?.fieldType
            val actualType = inspectDataType(fieldValue)
            if (expectedType != actualType) {
                return Pair(false, "Invalid data type at [$fieldName] field")
            }
        }
        return inspectValue(receive, relayPolicy)
    }
     */

    fun validateDataType(
        receive: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>
    ): Pair<Boolean, String?> {
        receive.forEach { (fieldName, fieldValue) ->
            val expectedType = relayPolicy.find { policy -> policy.fieldName == fieldName }?.fieldType
            val actualType = inspectDataType(fieldValue)
            if (expectedType != actualType) {
                return Pair(false, "Invalid data type at [$fieldName] field")
            }
        }

        val missingFields = relayPolicy.filterNot { field -> receive.containsKey(field.fieldName) }
        if (missingFields.isNotEmpty()) {
            val missingFieldNames = missingFields.joinToString(", ") { field -> field.fieldName }
            //LOG.warn("Missing field names: [$missingFieldNames]")
            return Pair(false, "Missing fields: [$missingFieldNames]")
        }

        return inspectValue(receive, relayPolicy)
    }



    private val LOG = LoggerFactory.getLogger(VerificationFactory::class.java)


}

fun main() = kotlinx.coroutines.runBlocking {
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

    val jsonEvent1 = invalidData.toJsonElementMap()
    val commandEvent1 = VerificationFactory().validateDataType(jsonEvent1, EventValidateField.entries.toTypedArray())
    println(commandEvent1)

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

    val jsonEvent2 = validData.toJsonElementMap()
    val commandEvent2 = VerificationFactory().validateDataType(jsonEvent2, EventValidateField.entries.toTypedArray())
    println(commandEvent2)

    val jsonREQ1 = """
        {
            "authors": [
              "8c3a51f90fe05d694a1efe95e0d31b1b00f3314029d708b120e7f8d4a983a89c",
              "e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3"
            ],
            "kinds": [1]
        }
    """.trimIndent()

    val jsonElemenREQ1 = jsonREQ1.toJsonElementMap()
    val commandREQ1 =
        VerificationFactory().validateDataType(jsonElemenREQ1, FiltersXValidateField.entries.toTypedArray())
    println(commandREQ1)

    val jsonREQ2 = """
        {
            "authors": [
              "e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3"
            ],
            "kinds": [4],
            "since": 1715181359
        }
    """.trimIndent()

    val jsonElemenREQ2 = jsonREQ2.toJsonElementMap()
    val commandREQ2 =
        VerificationFactory().validateDataType(jsonElemenREQ2, FiltersXValidateField.entries.toTypedArray())
    println(commandREQ2)
}