package org.lnwza007.database

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset


@Singleton
class RedisCacheFactory @Inject constructor(
    private val redisClient: RedisClient = RedisClient.create("redis://localhost:63790"),
    private val connection: StatefulRedisConnection<String, String> = redisClient.connect(),
    private val redisCommands: RedisCommands<String, String> = connection.sync()
) {

    private val LOG = LoggerFactory.getLogger(RedisCacheFactory::class.java)

    /**
     * ฟังก์ชันสำหรับ cache ข้อมูลลงใน Redis
     * @param key: คีย์ที่ใช้เก็บข้อมูลใน Redis
     * @param value: ข้อมูลที่จะถูกเก็บ
     * @param expirySeconds: เวลาที่ข้อมูลจะหมดอายุในหน่วยวินาที
     * @param serializer: Lambda expression สำหรับแปลงข้อมูลจาก T เป็น String
     * @return ค่า String ที่เก็บใน Redis หรือ null หากเกิดข้อผิดพลาด
     */
    suspend fun <T> setCache(
        key: String,
        value: T,
        expirySeconds: Long,
        serializer: (T) -> String
    ): String? = withContext(Dispatchers.IO) {
        // แปลงข้อมูลจาก T เป็น String โดยใช้ serializer ที่ส่งเข้ามา
        val serializedValue = serializer.invoke(value)
        // เก็บข้อมูลใน Redis พร้อมกำหนดเวลาหมดอายุ
        val result = redisCommands.setex(key, expirySeconds, serializedValue)

        // บันทึกลง log ว่าข้อมูลถูก cache แล้วหรือไม่
        if (result == "OK") {
            LOG.info("Cached data with key: $key")
        } else {
            LOG.error("Failed to cache data with key: $key")
        }
        return@withContext result
    }

    /**
     * ฟังก์ชันสำหรับดึงข้อมูลจาก Redis
     * @param key: คีย์ที่ใช้ค้นหาข้อมูลใน Redis
     * @param deserializer: Lambda expression สำหรับแปลงข้อมูลจาก String เป็น T
     * @return ข้อมูลที่ถูกดึงออกมาในรูปแบบของ T หรือ null หากไม่มีข้อมูลในคีย์นั้น
     */
    suspend fun <T> getCache(
        key: String,
        deserializer: (String) -> T
    ): T? = withContext(Dispatchers.IO) {
        // ดึงข้อมูลจาก Redis โดยใช้คีย์
        val serializedValue = redisCommands.get(key)

        // บันทึกลง log ว่าข้อมูลถูกดึงออกมาหรือไม่
        if (serializedValue != null) {
            LOG.info("Retrieved cached data with key: $key")  // ข้อมูลถูกดึงออกมาสำเร็จ
        } else {
            LOG.warn("No cached data found for key: $key")  // ไม่มีข้อมูลใน cache
        }
        return@withContext serializedValue?.let { deserializer.invoke(it) }
    }


    // ปิดการเชื่อมต่อกับ Redis เมื่อไม่ใช้งาน
    fun close() {
        connection.close()
        redisClient.shutdown()
    }

}

fun main() = runBlocking {
    // สร้าง RedisCacheFactory
    val redis = RedisCacheFactory()

    val html = File("src/main/resources/public/index.html").readText(Charset.defaultCharset())

    val json = File("src/main/resources/relay_information_document.json").readText()

    // ตัวอย่างการใช้งานฟังก์ชัน cacheData
    redis.setCache("html_file", html, 120) { value -> value }
    redis.setCache("json_file", json, 120) { value -> value }

    // ตัวอย่างการใช้งานฟังก์ชัน retrieveCachedData
    val retrievedHTMLData: String? = redis.getCache("html_file") { it }
    println("Retrieved data: $retrievedHTMLData")

    val retrievedJsonData: String? = redis.getCache("json_file") { it }
    println("Retrieved data: $retrievedJsonData")
    // ปิดการเชื่อมต่อกับ Redis เมื่อไม่ใช้งาน
    redis.close()
}