package org.lnwza007.relay.service.nip01

import jakarta.inject.Singleton
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.*
import org.slf4j.LoggerFactory

@Singleton
open class Validation {


    /**
     * ฟังก์ชัน checkFieldTypes ใช้ในการตรวจสอบประเภทของฟิลด์ข้อมูลว่าตรงกับนโยบายหรือไม่
     * @param field ฟิลด์ข้อมูลที่ต้องการตรวจสอบ
     * @param policyFieldType ชนิดข้อมูลของฟิลด์ที่นโยบายของ Relay กำหนด
     * @param policyCollectionType ชนิดข้อมูลแบบคอลเลคชันที่นโยบายของ Relay กำหนด (ถ้ามี)
     * @param nestedFieldType ประเภทของฟิลด์ที่อยู่ภายในคอลเลคชัน (ถ้ามี)
     * @return ผลลัพธ์จริงหรือเท็จว่าประเภทของฟิลด์ตรงกับนโยบายหรือไม่
     */
    private fun checkFieldTypes(
        field: JsonElement,
        policyFieldType: Class<*>,
        policyCollectionType: Class<*>?,
        nestedFieldType: Class<*>? = null
    ): Boolean {

        LOG.info("""
            << checkFieldTypes >>
            field: $field
            policyFieldType: $policyFieldType
        """.trimIndent())

        // ตรวจสอบว่าฟิลด์เป็นคอลเลคชันและมีฟิลด์ย่อยภายใน
        return when {
            // ถ้าฟิลด์เป็นคอลเลคชันและมีฟิลด์ย่อยที่กำหนด
            policyCollectionType == List::class.java && nestedFieldType != null -> {
                // เรียกใช้เมธอดเช็คฟิลด์ที่มีการซ้อนกัน
                checkNestedListField(field, nestedFieldType)
            }
            // ถ้าฟิลด์เป็นคอลเลคชันแต่ไม่มีฟิลด์ย่อยที่กำหนด
            policyCollectionType != null -> {
                // เรียกใช้เมธอดเช็คฟิลด์ของคอลเลคชัน
                checkListField(field, policyFieldType)
            }
            // ถ้าฟิลด์ไม่ใช่คอลเลคชัน
            else -> {
                // เรียกใช้เมธอดเช็คฟิลด์พื้นฐาน
                checkPrimitiveField(field, policyFieldType)
            }
        }
    }

    /**
     * ฟังก์ชัน checkNestedListField ใช้ในการตรวจสอบฟิลด์ข้อมูลที่เป็นคอลเลคชันแบบซ้อนกัน
     * @param field ฟิลด์ข้อมูลที่ต้องการตรวจสอบ
     * @param nestedFieldType ประเภทของฟิลด์ที่อยู่ภายในคอลเลคชัน
     * @return ผลลัพธ์จริงหรือเท็จว่าประเภทของฟิลด์ตรงกับนโยบายหรือไม่
     */
    private fun checkNestedListField(field: JsonElement, nestedFieldType: Class<*>): Boolean {
        // ตรวจสอบว่าฟิลด์เป็น JsonArray
        if (field !is JsonArray) return false
        // ตรวจสอบฟิลด์ภายในว่าตรงกับประเภทที่กำหนด
        return field.all { outerElement ->
            if (outerElement !is JsonArray) return false
            outerElement.all { checkPrimitiveField(it, nestedFieldType) }
        }
    }

    /**
     * ฟังก์ชัน checkListField ใช้ในการตรวจสอบฟิลด์ข้อมูลที่เป็นคอลเลคชันของฟิลด์ "tags"
     * @param field ฟิลด์ข้อมูลที่ต้องการตรวจสอบ
     * @param fieldType ประเภทของฟิลด์ที่นโยบายกำหนด
     * @return ผลลัพธ์จริงหรือเท็จว่าประเภทของฟิลด์ตรงกับนโยบายหรือไม่
     */
    private fun checkListField(field: JsonElement, fieldType: Class<*>): Boolean {
        if (field !is JsonArray) return false
        // ตรวจสอบว่าฟิลด์ภายในตรงกับประเภทที่กำหนด
        return field.all { checkPrimitiveField(it, fieldType) }
    }

    /**
     * ฟังก์ชัน checkPrimitiveField ใช้ในการตรวจสอบฟิลด์ข้อมูลที่เป็นชนิดพื้นฐาน
     * @param field ฟิลด์ข้อมูลที่ต้องการตรวจสอบ
     * @param fieldType ประเภทของฟิลด์ที่นโยบายกำหนด
     * @return ผลลัพธ์จะเป็น 'จริง' หรือ 'เท็จ' ขั้นกับว่าประเภทของฟิลด์ตรงกับนโยบายหรือไม่
     */
    private fun checkPrimitiveField(
        field: JsonElement,
        fieldType: Class<*>
    ): Boolean {

        //LOG.info("checkPrimitiveField: $fieldType")

        // ตรวจสอบประเภทรูปแบบข้อมูลของฟิลด์
        return when (fieldType) {
            String::class.java -> field is JsonPrimitive && field.isString
            Int::class.java -> field is JsonPrimitive && field.intOrNull != null
            Long::class.java -> field is JsonPrimitive && field.longOrNull != null && field.content.toLongOrNull() != null
            else -> false
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////


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
    ): Array<Any?> {
        // ตรวจสอบชื่อฟิลด์ว่าตรงกับนโยบายหรือไม่
        val (isFieldNamesValid, fieldNamesError) = checkFieldNames(map, relayPolicy)

        // แจ้งเตือนเมื่อมีฟิลด์ที่ไม่ตรงกับนโยบาย
        if (!isFieldNamesValid) {
            val errorMsg = "Invalid fields: $fieldNamesError"
            LOG.warn(errorMsg)
            return arrayOf(errorMsg, null)
        }

        // ตรวจสอบประเภทของข้อมูลว่าตรงกับนโยบายที่ Relay กำหนดหรือไม่
        val invalidTypes: List<Pair<String, String>> = relayPolicy.mapNotNull { enumField ->
            val field = map[enumField.fieldName]
            val fieldType = enumField.fieldType
            val fieldCollectionType = enumField.fieldCollectionType
            val nestedFieldType = (enumField as? NostrField)?.nestedFieldType
            if (field != null && !checkFieldTypes(field, fieldType, fieldCollectionType, nestedFieldType)) {
                enumField.fieldName to fieldType.simpleName
            } else {
                null
            }
        }

        // แจ้งเตือนเมื่อมีประเภทข้อมูลที่ไม่ตรงกับนโยบายที่ Relay กำหนด
        if (invalidTypes.isNotEmpty()) {
            val data = invalidTypes.joinToString(", ") { "${it.first} (${it.second})" }
            val errorMsg = "Invalid data type: $data"
            LOG.warn(errorMsg)
            return arrayOf(errorMsg, null)
        }

        // คืนค่า null หากรูปแบบหรือประเภทข้อมูลไม่ตรงกับนโยบายที่ Relay กำหนดไว้
        return arrayOf(null, converter(map))
    }


    private val LOG = LoggerFactory.getLogger(Validation::class.java)

}

