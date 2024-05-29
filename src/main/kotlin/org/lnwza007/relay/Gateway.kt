package org.lnwza007.relay

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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.lnwza007.relay.service.nip01.BasicProtocolFlow
import org.lnwza007.relay.service.nip11.RelayInformation
import org.slf4j.LoggerFactory

@ServerWebSocket("/")
class Gateway @Inject constructor(
    private val nip01: BasicProtocolFlow,
    private val nip11: RelayInformation
) {

    @OnOpen
    fun onOpen(session: WebSocketSession?, @Header(HttpHeaders.ACCEPT) accept: String?): HttpResponse<String>? {
        // ถ้าเป็นการเปิด WebSocket จะไม่มีการคืนค่า HttpResponse
        if (session != null) {
            LOG.info("${GREEN}open$RESET $session")
            return null
        }

        LOG.info("${YELLOW}accept: $RESET$accept ${BLUE}session: $RESET$session")

        val contentType = when {
            accept == "application/nostr+json" -> MediaType.APPLICATION_JSON
            else -> MediaType.TEXT_HTML
        }

        val data = runBlocking {
            nip11.loadRelayInfo(contentType)
        }

        return HttpResponse.ok(data).contentType(contentType)
    }


    @OnMessage
    fun onMessage(message: String, session: WebSocketSession) {
        val msg: JsonElement = Json.parseToJsonElement(message)

        if (msg.jsonArray.size < 2) {
            session.sendSync("Error: Invalid message format")
            session.close()
            return
        }

        when (msg.jsonArray[0].jsonPrimitive.content) {
            "REQ" -> nip01.onRequest(msg.jsonArray[2].toString(), msg.jsonArray[1].jsonPrimitive.content, session)
            "EVENT" -> nip01.onEvent(msg.jsonArray[1].toString(), session)
            "CLOSE" -> nip01.onClose(session)
            else ->session.sendSync("Unsupported message: $message")
        }
    }


    @OnClose
    fun onClose(session: WebSocketSession) {
        LOG.info("${PURPLE}close$RESET : $session")
    }


    private val LOG = LoggerFactory.getLogger(Gateway::class.java)

    companion object {
        const val RESET = "\u001B[0m"
        const val RED = "\u001B[31m"
        const val GREEN = "\u001B[32m"
        const val YELLOW = "\u001B[33m"
        const val BLUE = "\u001B[34m"
        const val PURPLE = "\u001B[35m"
        const val CYAN = "\u001B[36m"
        const val WHITE = "\u001B[37m"
    }

}
