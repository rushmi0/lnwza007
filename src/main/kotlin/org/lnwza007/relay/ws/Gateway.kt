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
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import org.lnwza007.relay.service.nip11.RelayInformation
import org.lnwza007.util.ShiftTo.toJsonElementMap
import org.slf4j.LoggerFactory

@ServerWebSocket("/")
class Gateway @Inject constructor(
    private val nip11: RelayInformation
) {

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

    @OnOpen
    fun onOpen(session: WebSocketSession?, @Header(HttpHeaders.ACCEPT) accept: String?): HttpResponse<String>? {
        // ถ้าเป็นการเปิด WebSocket จะไม่มีการคืนค่า HttpResponse
        if (session != null) {
            LOG.info("${GREEN}open${RESET} $session")
            return null
        }

        LOG.info("${YELLOW}accept: ${RESET}$accept ${BLUE}session: ${RESET}$session")

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


        val TYPE_OF_MESSAGES = arrayOf(
            "",
            "",
            ""
        )



        val jsonElement = Json.parseToJsonElement(message)
//        LOG.info("json Element 1: ${jsonElement.jsonArray[0]}")
//        LOG.info("json Element 2: ${jsonElement.jsonArray[1]}")
        //LOG.info("json Element 3: ${jsonElement.jsonArray[2]}")
        LOG.info("json Element : ${jsonElement.jsonArray}")
        LOG.info("json Element size: ${jsonElement.jsonArray.size}")

//        val map = jsonElement.jsonArray[2].toString().toJsonElementMap()
//        //val queue2 = buildUpdateQueue(map)
//        LOG.info("> ${map.entries.map { it.value }}")
//        val data = map.entries.map { it.value }
//        session.sendAsync(data.toString())
//
//        val type = jsonElement.jsonArray[0]
//        val subID = jsonElement.jsonArray[1]
//        LOG.info("type : $type")
//        LOG.info("subscription_id : $subID")

    }

    @OnClose
    fun onClose(session: WebSocketSession) {
        LOG.info("${PURPLE}close${RESET} : $session")
    }
}
