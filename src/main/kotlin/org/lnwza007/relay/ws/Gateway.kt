package org.lnwza007.relay.ws


import io.micronaut.http.HttpRequest
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.nio.charset.Charset


@ServerWebSocket("/")
class Gateway {


    @OnOpen
    fun onOpen(session: WebSocketSession?, @Header accept: String?, request: HttpRequest<*>?): MutableHttpResponse<String?>? {
        println("open : $session")
        println("request: $request")
        println("accept: $accept")

        val body = if (accept == "application/nostr+json") {
            javaClass.getResource("/nip-11.json")?.readText()
        } else {
            javaClass.getResourceAsStream("/public/index.html")?.readBytes()?.toString(Charset.defaultCharset())
        }

        val contentType = if (accept == "application/nostr+json") {
            MediaType.APPLICATION_JSON
        } else {
            MediaType.TEXT_HTML
        }

        return HttpResponse.ok(body).contentType(contentType)
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