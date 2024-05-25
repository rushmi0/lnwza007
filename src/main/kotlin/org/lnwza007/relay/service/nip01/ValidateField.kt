package org.lnwza007.relay.service.nip01

import jakarta.inject.Singleton
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.*

@Singleton
open class ValidateField {

    /**
     * ฟังก์ชัน checkFieldNames ใช้ในการตรวจสอบชื่อฟิลด์ข้อมูลว่าตรงกับนโยบายหรือไม่
     * @param receive ข้อมูลที่ต้องการตรวจสอบ
     * @param policy นโยบายการตรวจสอบฟิลด์
     * @return ผลลัพธ์จริงหรือเท็จว่าชื่อฟิลด์ตรงกับนโยบายหรือไม่
     */
    private fun checkFieldNames(
        receive: Map<String, JsonElement>,
        policy: Array<out EnumField>
    ): Boolean {
        val allowedFields: Set<String> = policy.map { it.fieldName }.toSet()
        return receive.keys.all { it in allowedFields }
    }

    /**
     * ฟังก์ชัน mapToObject ใช้ในการแปลงข้อมูล JSON เป็นออบเจ็กต์ตามนโยบายที่กำหนด
     * @param map ข้อมูล JSON ที่ต้องการแปลง
     * @param policy นโยบายการแปลงข้อมูล
     * @param converter ฟังก์ชันที่ใช้ในการแปลงข้อมูล
     * @return ออบเจ็กต์ที่ได้จากการแปลงข้อมูล หรือ null หากข้อมูลไม่ตรงกับนโยบายที่ Relay กำหนด
     */
    fun <T> mapToObject(
        map: Map<String, JsonElement>,
        policy: Array<out EnumField>,
        converter: (Map<String, JsonElement>) -> T
    ): T? {
        val isValid: Boolean = checkFieldNames(map, policy)
        return if (isValid) converter(map) else null
    }

}
