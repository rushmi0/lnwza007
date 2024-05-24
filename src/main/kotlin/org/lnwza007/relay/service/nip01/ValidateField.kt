package org.lnwza007.relay.service.nip01

import jakarta.inject.Singleton
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.*

@Singleton
open class ValidateField {

    private fun areFieldsValid(
        data: Map<String, JsonElement>,
        validateFields: Array<out EnumField>
    ): Boolean {
        val validFieldNames = validateFields.map { it.fieldName }.toSet()
        return data.keys.all { it in validFieldNames }
    }

    fun <T> mapToObject(
        map: Map<String, JsonElement>,
        validateFields: Array<out EnumField>,
        converter: (Map<String, JsonElement>) -> T
    ): T? {
        val isValid = areFieldsValid(map, validateFields)
        return if (isValid) converter(map) else null
    }


}
