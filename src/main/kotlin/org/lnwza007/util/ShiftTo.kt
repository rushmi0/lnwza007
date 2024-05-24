package org.lnwza007.util

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import fr.acinq.secp256k1.Hex
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import java.security.MessageDigest
import kotlin.random.Random

object ShiftTo {

    /**
     * ฟังก์ชัน randomBytes ใช้ในการสร้างอาร์เรย์ไบต์สุ่มขนาดที่กำหนด
     * @param size ขนาดของอาร์เรย์ไบต์ที่ต้องการสร้าง
     * @return อาร์เรย์ไบต์ที่สร้างขึ้น
     */
    fun randomBytes(size: Int): ByteArray = Random.nextBytes(size)

    /**
     * ฟังก์ชัน toHex ใช้ในการแปลง ByteArray เป็นสตริงที่เป็นเลขฐาน 16
     * @return สตริงที่เป็นเลขฐาน 16
     */
    fun ByteArray.toHex(): String = Hex.encode(this)

    /**
     * ฟังก์ชัน fromHex ใช้ในการแปลงสตริงที่เป็นฐาน 16 เป็น ByteArray
     * @return อาร์เรย์ไบต์ที่ถูกแปลงจากสตริงเลขฐาน 16
     */
    fun String.fromHex(): ByteArray = Hex.decode(this)

    /**
     * ฟังก์ชัน toSha256 ใช้ในการคำนวณ Hash SHA-256 ของ ByteArray
     * @return อาร์เรย์ไบต์
     */
    fun ByteArray.toSha256(): ByteArray {
        return MessageDigest.getInstance("SHA-256").digest(this)
    }

    /**
     * ฟังก์ชัน toSha256 ใช้ในการคำนวณ Hash SHA-256 ของสตริง
     * @return สตริงที่เป็นเลขฐาน 16
     */
    fun String.toSha256(): String {
        return toByteArray().toSha256().toHex()
    }

    /**
     * ฟังก์ชัน toJsonString ใช้ในการแปลงข้อมูลใดๆเป็นสตริง JSON
     * @return สตริง JSON ที่เป็นผลลัพธ์จากการแปลง Object
     */
    fun Any.toJsonString() : String {
        return jacksonObjectMapper().writeValueAsString(this)
    }

    /**
     * ฟังก์ชัน toJsonElementMap ใช้ในการแปลงสตริง JSON เป็น Map ของ JsonElement
     * @return Map ของ JsonElement ที่เป็นผลลัพธ์จากการแปลงสตริง JSON
     */
    fun String.toJsonElementMap(): Map<String, JsonElement> {
        val option = Json { isLenient = true }
        val jsonElement = option.parseToJsonElement(this)
        return jsonElement.jsonObject
    }

}