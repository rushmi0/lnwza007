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
import org.lnwza007.relay.service.nip01.*
import org.lnwza007.relay.service.nip01.command.DetectCommand.parseCommand
import org.lnwza007.relay.service.nip01.command.AUTH
import org.lnwza007.relay.service.nip01.command.CLOSE
import org.lnwza007.relay.service.nip01.command.EVENT
import org.lnwza007.relay.service.nip01.command.REQ
import org.lnwza007.relay.service.nip01.response.RelayResponse
import org.lnwza007.relay.service.nip11.RelayInformation
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@ServerWebSocket("/")
class Gateway @Inject constructor(
    private val nip01: BasicProtocolFlow,
    private val nip11: RelayInformation
) {

    @OnOpen
    fun onOpen(session: WebSocketSession?, @Header(HttpHeaders.ACCEPT) accept: String?): HttpResponse<String>? {
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

        try {
            val (command, validationResult) = parseCommand(message)
            val (status, warning) = validationResult
            when (command) {

                is EVENT -> {
                    LOG.info("event: ${command.event}")
                    RelayResponse.OK(eventId = command.event.id!!, isSuccess = status, message = warning).toClient(session)
                }

                is REQ -> {
                    if (status) {
                        LOG.info("request for subscription ID: ${command.subscriptionId} with filters: ${command.filtersX}")
                        RelayResponse.EOSE(subscriptionId = command.subscriptionId).toClient(session)
                    } else {
                        RelayResponse.NOTICE(warning).toClient(session)
                    }
                }

                is CLOSE -> {
                    LOG.info("close request for subscription ID: ${command.subscriptionId} on $session")
                    RelayResponse.CLOSED(subscriptionId = command.subscriptionId, message = "").toClient(session)
                }

                else -> {
                    LOG.warn("Unknown command")
                    RelayResponse.NOTICE("Unknown command").toClient(session)
                }
            }

        } catch (e: Exception) {
            LOG.error("Failed to handle command: ${e.message}")
            RelayResponse.NOTICE("ERROR: ${e.message}").toClient(session)
        }

    }


    @OnClose
    fun onClose(session: WebSocketSession) {
        LOG.info("${PURPLE}close: ${RESET}$session")
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
