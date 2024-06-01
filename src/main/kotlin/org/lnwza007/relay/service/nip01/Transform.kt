package org.lnwza007.relay.service.nip01

import jakarta.inject.Singleton
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.EventValidateField
import org.lnwza007.relay.modules.FiltersX
import org.lnwza007.relay.modules.FiltersXValidateField

/**
 * Transform ใช้ในการแปลงข้อมูล JSON เป็นออบเจ็กต์ที่สามารถนำไปใช้ต่อได้ง่าย
 */
@Singleton
object Transform : VerificationFactory() {

    /**
     * ฟังก์ชัน convertToFiltersXObject ใช้ในการแปลงข้อมูล JSON เป็นออบเจ็กต์ FiltersX
     * @param field ข้อมูล JSON ที่ต้องการแปลง
     * @return ออบเจ็กต์ FiltersX ที่ได้จากการแปลงข้อมูล
     */
    private fun convertToFiltersXObject(field: Map<String, JsonElement>): FiltersX {
        return FiltersX(
            ids = field["ids"]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet(),
            authors = field["authors"]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet(),
            kinds = field["kinds"]?.jsonArray?.map { it.jsonPrimitive.long }?.toSet(),
            since = field["since"]?.jsonPrimitive?.longOrNull,
            until = field["until"]?.jsonPrimitive?.longOrNull,
            limit = field["limit"]?.jsonPrimitive?.longOrNull,
            search = field["search"]?.jsonPrimitive?.contentOrNull
        )
    }

    /**
     * ฟังก์ชัน convertToEventObject ใช้ในการแปลงข้อมูล JSON เป็นออบเจ็กต์ Event
     * @param field ข้อมูล JSON ที่ต้องการแปลง
     * @return ออบเจ็กต์ Event ที่ได้จากการแปลงข้อมูล
     */
    private fun convertToEventObject(field: Map<String, JsonElement>): Event {
        return Event(
            id = field["id"]?.jsonPrimitive?.contentOrNull,
            pubkey = field["pubkey"]?.jsonPrimitive?.contentOrNull,
            createAt = field["created_at"]?.jsonPrimitive?.longOrNull,
            content = field["content"]?.jsonPrimitive?.contentOrNull,
            kind = field["kind"]?.jsonPrimitive?.longOrNull,
            tags = field["tags"]?.jsonArray?.map {
                it.jsonArray.map { tag -> tag.jsonPrimitive.content }
            },
            signature = field["sig"]?.jsonPrimitive?.contentOrNull
        )
    }

    /**
     * ฟังก์ชัน toFiltersX (Extension function) ใช้ในการแปลงข้อมูล JSON เป็นออบเจ็กต์ FiltersX
     * @receiver Map<String, JsonElement> ข้อมูล JSON ที่ต้องการแปลง
     * @return คู่ของข้อความแจ้งเตือนและออบเจ็กต์ FiltersX หากข้อมูลไม่ตรงกับนโยบาย และ null ถ้าไม่มีข้อความแจ้งเตือน
     */
    fun Map<String, JsonElement>.toFiltersX(): Pair<String?, FiltersX?> {
        // เรียกใช้ฟังก์ชัน mapToObject เพื่อแปลงข้อมูล JSON เป็น FiltersX ตามนโยบายที่กำหนด
        val (status, message, objectFiltersX) = mapToObject(
            this,
            FiltersXValidateField.entries.toTypedArray(),
            ::convertToFiltersXObject
        )
        // คืนค่าคู่ของข้อความแจ้งเตือนและออบเจ็กต์ FiltersX
        return Pair(message, objectFiltersX)
    }

    /**
     * ฟังก์ชัน toEvent (Extension function) ใช้ในการแปลงข้อมูล JSON เป็นออบเจ็กต์ Event
     * @receiver Map<String, JsonElement> ข้อมูล JSON ที่ต้องการแปลง
     * @return คู่ของข้อความแจ้งเตือนและออบเจ็กต์ Event หากข้อมูลไม่ตรงกับนโยบาย และ null ถ้าไม่มีข้อความแจ้งเตือน
     */
    fun Map<String, JsonElement>.toEvent(): Pair<String?, Event?> {
        // เรียกใช้ฟังก์ชัน mapToObject เพื่อแปลงข้อมูล JSON เป็น Event ตามนโยบายที่กำหนด
        val (status, message, objectEvent) = mapToObject(
            this,
            EventValidateField.entries.toTypedArray(),
            ::convertToEventObject
        )
        // คืนค่าคู่ของข้อความแจ้งเตือนและออบเจ็กต์ Event
        return Pair(message, objectEvent)
    }


}
