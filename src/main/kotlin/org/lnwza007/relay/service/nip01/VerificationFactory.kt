package org.lnwza007.relay.service.nip01

import jakarta.inject.Singleton
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.Event
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
        val (isFieldNamesValid: Boolean, fieldNamesError: String?) = checkFieldNames(receive, relayPolicy)
        val (isDataValid: Boolean, dataError: String?) = validateDataType(receive, relayPolicy)

        val status: Boolean = isFieldNamesValid && isDataValid

        if (!isFieldNamesValid) {
            return Pair(isFieldNamesValid, fieldNamesError)
        }

        if (!isDataValid) {
            return Pair(isDataValid, dataError)
        }

        return Pair(status, "")
    }

    private fun checkFieldNames(
        receive: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>
    ): Pair<Boolean, String> {
        val allowedFields: Set<String> = relayPolicy.map { it.fieldName }.toSet()
        val invalidFields: List<String> = receive.keys.filter { it !in allowedFields }
        val msgError = "unsupported: [${invalidFields.joinToString(", ")}] fields"

        return if (invalidFields.isEmpty()) Pair(true, "") else Pair(false, msgError)
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
            is JsonArray -> ArrayList::class.java
            is JsonObject -> Map::class.java
            else -> receive.toString()::class.java
        }
    }

    private fun inspectValue(
        receive: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>
    ): Pair<Boolean, String> {
        return when {

            relayPolicy.isArrayOf<FiltersXValidateField>() -> {
                val obj = convertToFiltersXObject(receive)
                Pair(false, "Not yet implemented")
            }

            relayPolicy.isArrayOf<EventValidateField>() -> {
                val obj = convertToEventObject(receive)

                val id = generateId(obj)
                if (!Schnorr.verify(id, obj.pubkey!!, obj.signature!!)) {
                    LOG.info("invalid: signature")
                    return Pair(false, "invalid: signature")
                }

                if (obj.id != id) {
                    LOG.info("invalid: event id")
                    return Pair(false, "invalid: event id")
                }

                Pair(true, "")
            }
            else -> Pair(false, "unsupported: relay policy")
        }
    }

    private inline fun <reified T> Array<*>.isArrayOf(): Boolean = all { it is T }

    fun validateDataType(
        receive: Map<String, JsonElement>,
        relayPolicy: Array<out NostrField>
    ): Pair<Boolean, String> {
        receive.forEach { (fieldName, fieldValue) ->
            val expectedType = relayPolicy.find { policy -> policy.fieldName == fieldName }?.fieldType
            val actualType = inspectDataType(fieldValue)
            if (expectedType != actualType) {
                //LOG.info("Invalid data type at [$fieldName] field")
                return Pair(false, "invalid: data type at [$fieldName] field")
            }
        }

        if (relayPolicy.isArrayOf<EventValidateField>()) {
            val missingFields = relayPolicy.filterNot { field -> receive.containsKey(field.fieldName) }
            if (missingFields.isNotEmpty()) {
                val missingFieldNames = missingFields.joinToString(", ") { field -> field.fieldName }
                //LOG.info("Missing fields: [$missingFieldNames]")
                return Pair(false, "invalid: missing fields: [$missingFieldNames]")
            }
        }


        return inspectValue(receive, relayPolicy)
    }

    private val LOG = LoggerFactory.getLogger(VerificationFactory::class.java)

}
