package org.lnwza007.relay.ws


import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Header
import io.micronaut.websocket.WebSocketSession
import io.micronaut.websocket.annotation.OnClose
import io.micronaut.websocket.annotation.OnMessage
import io.micronaut.websocket.annotation.OnOpen
import io.micronaut.websocket.annotation.ServerWebSocket
import jakarta.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory


@ServerWebSocket("/")
class Gateway @Inject constructor(
    private val nip11: RelayInformation
) {

    private val LOG = LoggerFactory.getLogger(Gateway::class.java)

    @OnOpen
    fun onOpen(session: WebSocketSession?, @Header(HttpHeaders.ACCEPT) accept: String?): HttpResponse<String>? {

        if (session != null) {
            LOG.info("open $session}")
        } else {
            LOG.info("accept: $accept session: $session")
        }


        val contentType = when {
            accept == "application/nostr+json" -> MediaType.APPLICATION_JSON
            session == null -> MediaType.TEXT_HTML
            else -> MediaType.TEXT_PLAIN
        }

        val data = runBlocking {
            nip11.loadRelayInfo(contentType)
        }

        return HttpResponse.ok(data).contentType(contentType)
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


}