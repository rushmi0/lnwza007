package org.lnwza007.relay.service.nip01

import jakarta.inject.Singleton
import kotlinx.serialization.json.*
import org.lnwza007.relay.policy.EventValidateField
import org.lnwza007.relay.policy.FiltersXValidateField
import org.lnwza007.relay.policy.NostrField
import org.lnwza007.relay.service.nip01.Transform.convertToEventObject
import org.lnwza007.relay.service.nip01.Transform.convertToFiltersXObject
import org.lnwza007.util.Schnorr
import org.lnwza007.util.ShiftTo.generateId
import org.slf4j.LoggerFactory

@Singleton
open class VerificationFactory {


    fun validateJsonElement(
        receive: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>
    ): Pair<Boolean, String> {
        val (isFieldNamesValid, fieldNamesError) = checkFieldNames(receive, relayPolicy)
        val (isDataValid, dataError) = validateDataType(receive, relayPolicy)

        return when {
            !isFieldNamesValid -> Pair(false, fieldNamesError)
            !isDataValid -> Pair(false, dataError)
            else -> Pair(true, "")
        }
    }


    private fun checkFieldNames(
        receive: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>
    ): Pair<Boolean, String> {
        val allowedFields = relayPolicy.map { it.fieldName }.toSet()
        val invalidFields: List<String> = receive.keys.filter { it !in allowedFields && !isValidTag(it) }

        val errorMessage = if (invalidFields.isNotEmpty()) buildErrorMessage(invalidFields) else ""

        return if (errorMessage.isEmpty()) Pair(true, "") else Pair(false, errorMessage)
    }

    private fun isValidTag(tag: String): Boolean {
        return tag.startsWith("#") && tag.length == 2 && setOf("#e", "#d", "#a", "#p").contains(tag)
    }

    private fun buildErrorMessage(invalidFields: List<String>): String {
        return "unsupported: ${invalidFields.joinToString(", ")} fields"
    }

    fun validateDataType(
        receive: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>
    ): Pair<Boolean, String> {
        receive.forEach { (fieldName, fieldValue) ->
            val expectedType = relayPolicy.find { policy -> policy.fieldName == fieldName }?.fieldType
            val actualType = inspectDataType(fieldValue)

            if (expectedType != actualType) {
                val errorMessage = "invalid data type at [$fieldName] field. Expected: $expectedType"
                LOG.info(errorMessage)
                return Pair(false, errorMessage)
            }
        }

        if (relayPolicy.isArrayOfPolicy<EventValidateField>()) {
            val missingFields = relayPolicy.filterNot { field -> receive.containsKey(field.fieldName) }
            if (missingFields.isNotEmpty()) {
                val missingFieldNames = missingFields.joinToString(", ") { field -> field.fieldName }
                val errorMessage = "invalid: missing fields: [$missingFieldNames]"
                LOG.info(errorMessage)
                return Pair(false, errorMessage)
            }
        }

        return inspectValue(receive, relayPolicy)
    }

    private fun inspectDataType(receive: JsonElement): Class<*> {
        return when (receive) {
            is JsonPrimitive -> determinePrimitiveType(receive)
            is JsonArray -> ArrayList::class.java
            is JsonObject -> Map::class.java
            else -> receive.toString()::class.java
        }
    }

    private fun determinePrimitiveType(receive: JsonPrimitive): Class<*> {
        return when {
            receive.isString -> String::class.java
            receive.booleanOrNull != null -> Boolean::class.java
            receive.longOrNull != null -> Long::class.java
            receive.doubleOrNull != null -> Double::class.java
            else -> Any::class.java
        }
    }

    private fun inspectValue(
        receive: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>
    ): Pair<Boolean, String> {
        return when {
            relayPolicy.isArrayOfPolicy<FiltersXValidateField>() -> validateFiltersX(receive)
            relayPolicy.isArrayOfPolicy<EventValidateField>() -> validateEvent(receive)
            else -> Pair(false, "unsupported: relay policy")
        }
    }

    private fun validateFiltersX(receive: Map<String, JsonElement>): Pair<Boolean, String> {
        val obj = convertToFiltersXObject(receive)
        LOG.info("receive: $receive")
        LOG.info("FiltersX: $obj")

        return Pair(true, "Not yet implemented")
    }

    private fun validateEvent(receive: Map<String, JsonElement>): Pair<Boolean, String> {
        val obj = convertToEventObject(receive)

        val id = generateId(obj)
        if (!Schnorr.verify(id, obj.pubkey!!, obj.signature!!)) {
            val errorMessage = "invalid: signature"
            LOG.info(errorMessage)
            return Pair(false, errorMessage)
        }

        if (obj.id != id) {
            val errorMessage = "invalid: event id"
            LOG.info(errorMessage)
            return Pair(false, errorMessage)
        }

        return Pair(true, "")
    }

    private inline fun <reified T> Array<*>.isArrayOfPolicy(): Boolean = all { it is T }

    private val LOG = LoggerFactory.getLogger(VerificationFactory::class.java)

}
