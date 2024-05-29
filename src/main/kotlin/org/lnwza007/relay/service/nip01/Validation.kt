package org.lnwza007.relay.service.nip01

import jakarta.inject.Singleton
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.*
import org.slf4j.LoggerFactory

@Singleton
open class Validation {

    /**
     * ฟังก์ชัน checkFieldNames ใช้ในการตรวจสอบชื่อฟิลด์ข้อมูลว่าตรงกับนโยบายหรือไม่
     * @param receive ข้อมูลที่ต้องการตรวจสอบ
     * @param policy นโยบายการตรวจสอบฟิลด์
     * @return ผลลัพธ์จริงหรือเท็จว่าชื่อฟิลด์ตรงกับนโยบายหรือไม่
     */
    private fun checkFieldNames(
        receive: Map<String, JsonElement>,
        policy: Array<out NostrField>
    ): Pair<Boolean, String?> {
        val allowedFields: Set<String> = policy.map { it.fieldName }.toSet()
        val invalidFields: List<String> = receive.keys.filter { it !in allowedFields }
        return if (invalidFields.isEmpty()) Pair(true, null) else Pair(
            false,
            invalidFields.joinToString(", ")
        )
    }

    private fun checkFieldTypes(
        field: JsonElement,
        policyFieldType: Class<*>,
        policyCollectionType: Class<*>?,
        nestedFieldType: Class<*>? = null
    ): Boolean {
        return when {
            policyCollectionType == List::class.java && nestedFieldType != null -> {
                checkNestedListField(field, nestedFieldType)
            }
            policyCollectionType != null -> {
                checkListField(field, policyFieldType)
            }
            else -> {
                checkPrimitiveField(field, policyFieldType)
            }
        }
    }

    private fun checkNestedListField(field: JsonElement, nestedFieldType: Class<*>): Boolean {
        if (field !is JsonArray) return false
        return field.all { outerElement ->
            if (outerElement !is JsonArray) return false
            outerElement.all { checkPrimitiveField(it, nestedFieldType) }
        }
    }

    private fun checkListField(field: JsonElement, fieldType: Class<*>): Boolean {
        if (field !is JsonArray) return false
        return field.all { checkPrimitiveField(it, fieldType) }
    }

    private fun checkPrimitiveField(field: JsonElement, fieldType: Class<*>): Boolean {
        return when (fieldType) {
            String::class.java -> field is JsonPrimitive && field.isString
            Int::class.java -> field is JsonPrimitive && field.intOrNull != null
            Long::class.java -> field is JsonPrimitive && field.longOrNull != null
            else -> false
        }
    }


    fun <T> mapToObject(
        map: Map<String, JsonElement>,
        policy: Array<out NostrField>,
        converter: (Map<String, JsonElement>) -> T
    ): T? {
        val (isFieldNamesValid, fieldNamesError) = checkFieldNames(map, policy)
        if (!isFieldNamesValid) {
            LOG.warn("Invalid fields: $fieldNamesError")
        }

        val invalidTypes = policy.mapNotNull { nostrField ->
            val field = map[nostrField.fieldName]
            val fieldType = nostrField.fieldType
            val fieldCollectionType = nostrField.fieldCollectionType
            val nestedFieldType = (nostrField as? NostrField)?.nestedFieldType
            if (field != null && !checkFieldTypes(field, fieldType, fieldCollectionType, nestedFieldType)) {
                nostrField.fieldName to fieldType.simpleName
            } else {
                null
            }
        }

        if (invalidTypes.isNotEmpty()) {
            LOG.warn("Invalid data type: ${invalidTypes.joinToString(", ") { "${it.first} (${it.second})" }}")
        }

        return if (isFieldNamesValid && invalidTypes.isEmpty()) converter(map) else null
    }


    private val LOG = LoggerFactory.getLogger(Validation::class.java)

}


