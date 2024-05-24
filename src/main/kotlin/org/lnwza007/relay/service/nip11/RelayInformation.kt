package org.lnwza007.relay.service.nip11

import io.micronaut.http.MediaType
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.lnwza007.database.RedisCacheFactory
import java.io.File
import java.nio.charset.Charset
import jakarta.inject.Singleton

@Singleton
class RelayInformation @Inject constructor(private val redis: RedisCacheFactory) {

    /**
     * ฟังก์ชันสำหรับดึงข้อมูล relay information (NIP-11)
     * @param contentType: ประเภทของเนื้อหาที่ต้องการ (application/json หรือ text/html)
     * @return ข้อมูล relay information ที่ถูกดึงจาก Redis cache หรือไฟล์ระบบ
     */
    suspend fun loadRelayInfo(contentType: String): String = withContext(Dispatchers.IO) {
//        // ถ้า contentType เป็น text/plain ไม่ทำการ cache ข้อมูล
//        if (contentType == MediaType.TEXT_PLAIN) {
//            return@withContext loadContent(contentType)
//        }

        // ดึงข้อมูลจาก Redis cache โดยใช้ contentType เป็น key
        redis.getCache(contentType) { it } ?: run {
            // หากไม่มีข้อมูลใน cache ให้โหลดจากไฟล์ระบบ
            val data = loadContent(contentType)
            // แคชข้อมูลที่โหลดมาใหม่ลง Redis พร้อมตั้งเวลาอายุเป็น 200 วินาที
            redis.setCache(contentType, data, 200) { it }
            data
        }
    }

    /**
     * ฟังก์ชันสำหรับโหลดเนื้อหาจากไฟล์ตามประเภทของ contentType
     * @param contentType: ประเภทของเนื้อหาที่ต้องการ
     * @return ข้อมูลที่โหลดจากไฟล์
     */
    private fun loadContent(contentType: String): String {
        return if (contentType == MediaType.APPLICATION_JSON) {
            // ถ้า contentType เป็น application/json ให้โหลดไฟล์ JSON
            loadFromFile("src/main/resources/relay_information_document.json")
        } else {
            // ถ้า contentType เป็น text/html ให้โหลดไฟล์ HTML
            loadFromFile("src/main/resources/public/index.html")
        }
    }

    /**
     * ฟังก์ชันสำหรับอ่านข้อมูลจากไฟล์
     * @param path: เส้นทางของไฟล์ที่จะอ่าน
     * @return ข้อมูลที่อ่านจากไฟล์
     */
    private fun loadFromFile(path: String): String = File(path).readText(Charset.defaultCharset())
}
