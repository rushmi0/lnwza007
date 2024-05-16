package org.lnwza007.relay.ws


import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Header
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.charset.Charset


@ServerWebSocket("/")
class Gateway(
    private val redis: RedisCacheFactory
) {

    @OnOpen
    suspend fun onOpen(session: WebSocketSession?, @Header accept: String?): MutableHttpResponse<String?>? {
        val body: String = if (accept == "application/nostr+json") {
            File("src/main/resources/relay_information_document.json").readText()
        } else {
            File("src/main/resources/public/index.html").readText(Charset.defaultCharset())

        }

        val contentType: String = if (accept == "application/nostr+json") {
            MediaType.APPLICATION_JSON
        } else {
            MediaType.TEXT_HTML
        }

        return HttpResponse.ok(body).contentType(contentType)
    }


    // ฟังก์ชันสำหรับอ่านไฟล์จาก resource โดยใช้ coroutines และส่งคืนข้อมูลเป็น string
    suspend fun readResource(path: String): String {
        return withContext(Dispatchers.IO) {
            // อ่านข้อมูลจากไฟล์และแปลงเป็น string
            javaClass.getResourceAsStream(path)?.readBytes()?.toString(Charset.defaultCharset()) ?: ""
        }
    }


    @OnMessage
    fun onMessage(message: String, session: WebSocketSession) {
        LOG.info("Received message: \n$message")


        val messageObj = Json.decodeFromString<List<String>>(message)

        CoroutineScope(Dispatchers.IO).launch {
            flow {
                emit(messageObj)
            }
                .filter { it.isNotEmpty() && it[0] == "EVENT" }
                .map { listOf("OK", it[1], "Websocket") }
                .map { Json.encodeToString(it) }
                .collect { response ->
                    try {
                        session.sendSync(response)
                    } catch (e: Exception) {
                        LOG.error("Failed to send message", e)
                    }
                }
        }
    }

    @OnClose
    fun onClose(session: WebSocketSession) {
        LOG.info("close : $session")
    }

    private val LOG = LoggerFactory.getLogger(Gateway::class.java)

}