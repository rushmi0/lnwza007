package org.lnwza007.relay.ws

import io.lettuce.core.RedisClient
import io.lettuce.core.api.StatefulRedisConnection
import io.lettuce.core.api.sync.RedisCommands
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.Charset

@Singleton
class RedisCacheFactory @Inject constructor(
    private val redisClient: RedisClient = RedisClient.create("redis://localhost:6379"),
    private val connection: StatefulRedisConnection<String, String> = redisClient.connect(),
    private val redisCommands: RedisCommands<String, String> = connection.sync()
) {

    // Lambda expression เพื่อ cache ข้อมูลลงใน Redis
    suspend fun <T> cacheData(
        key: String,
        value: T,
        expirySeconds: Long,
        serializer: (T) -> String
    ) : String? = withContext(Dispatchers.IO) {
        val serializedValue = serializer.invoke(value)
        redisCommands.setex(key, expirySeconds, serializedValue)
    }

    // Lambda expression เพื่อดึงข้อมูลจาก Redis
    suspend fun <T> retrieveCachedData(
        key: String,
        deserializer: (String) -> T
    ) : T? = withContext(Dispatchers.IO) {
        val serializedValue = redisCommands.get(key)
        return@withContext serializedValue?.let { deserializer.invoke(it) }
    }

    // ปิดการเชื่อมต่อกับ Redis เมื่อไม่ใช้งาน
    fun close() {
        connection.close()
        redisClient.shutdown()
    }

}

fun main() {
    // สร้าง RedisCacheFactory
    val redisCacheFactory = RedisCacheFactory()

    val html = File("src/main/resources/public/index.html").readText(Charset.defaultCharset())

    val json = File("src/main/resources/relay_information_document.json").readText()

    // ตัวอย่างการใช้งานฟังก์ชัน cacheData
//    runBlocking {
//        redisCacheFactory.cacheData("html_file", html, 60) { value -> value }
//        redisCacheFactory.cacheData("json_file", json, 60) { value -> value }
//    }

    // ตัวอย่างการใช้งานฟังก์ชัน retrieveCachedData
    runBlocking {
        val retrievedHTMLData: String? = redisCacheFactory.retrieveCachedData("html_file") { it }
        println("Retrieved data: $retrievedHTMLData")

        val retrievedJsonData: String? = redisCacheFactory.retrieveCachedData("json_file") { it }
        println("Retrieved data: $retrievedJsonData")
    }

    // ปิดการเชื่อมต่อกับ Redis เมื่อไม่ใช้งาน
    redisCacheFactory.close()
}