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
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.FiltersX
import org.lnwza007.relay.policy.EventValidateField
import org.lnwza007.relay.policy.FiltersXValidateField
import org.lnwza007.relay.service.nip01.*
import org.lnwza007.relay.service.nip01.response.RelayResponse
import org.lnwza007.relay.service.nip11.RelayInformation
import org.lnwza007.util.ShiftTo.toJsonElementMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@ServerWebSocket("/")
class Gateway @Inject constructor(
    private val nip01: BasicProtocolFlow,
    private val nip11: RelayInformation,
    private val validate: VerificationFactory
) {

    @OnOpen
    fun onOpen(session: WebSocketSession?, @Header(HttpHeaders.ACCEPT) accept: String?): HttpResponse<String>? {
        // ถ้าเป็นการเปิด WebSocket จะไม่มีการคืนค่า HttpResponse
        session?.let {
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
        LOG.info("message: \n$message")

        val (command, validationResult) = parseCommand(message)
        val (status, warning) = validationResult
        when (command) {
            is EVENT -> {
                LOG.info("event: ${command.event}")
                RelayResponse.OK(eventId = command.event.id!!, isSuccess = status, message = warning).toClient(session)
            }
            is REQ -> {
                LOG.info("request for subscription ID: ${command.subscriptionId} with filters: ${command.filtersX}")
                if (status) {
                    RelayResponse.EOSE(subscriptionId = command.subscriptionId).toClient(session)
                } else {
                    RelayResponse.NOTICE(validationResult.second).toClient(session)
                }
            }
            is CLOSE -> {
                LOG.info("close request for subscription ID: ${command.subscriptionId}")
                RelayResponse.CLOSED(subscriptionId = command.subscriptionId).toClient(session)
            }
            else -> {
                LOG.warn("Unknown command")
            }
        }

        /*
        try {

        } catch (e: Exception) {
            LOG.error("Failed to handle command: ${e.message}")
            RelayResponse.NOTICE("Invalid command: ${e.message}").toClient(session)
        }

         */

    }



                        @OnClose
    fun onClose(session: WebSocketSession) {

    }

    private val LOG: Logger = LoggerFactory.getLogger(Gateway::class.java)

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
