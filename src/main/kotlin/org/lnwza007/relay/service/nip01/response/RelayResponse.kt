package org.lnwza007.relay.service.nip01.response

import io.micronaut.websocket.WebSocketSession
import org.lnwza007.relay.modules.Event
import org.lnwza007.util.ShiftTo.toJsonString
import org.slf4j.Logger
import org.slf4j.LoggerFactory

sealed class RelayResponse<out T> {

    data class EVENT(val subscriptionId: String, val event: Event) : RelayResponse<Unit>()

    data class OK(val eventId: String, val isSuccess: Boolean, val message: String = "") : RelayResponse<Unit>()

    data class EOSE(val subscriptionId: String) : RelayResponse<Unit>()

    data class CLOSED(val subscriptionId: String, val message: String = "") : RelayResponse<Unit>()

    data class NOTICE(val message: String = "") : RelayResponse<Unit>()

    fun toJson(): String {
        return when (this) {
            is EVENT -> listOf("EVENT", subscriptionId, event).toJsonString()
            is OK -> listOf("OK", eventId, isSuccess, message).toJsonString()
            is EOSE -> listOf("EOSE", subscriptionId).toJsonString()
            is CLOSED -> listOf("CLOSED", subscriptionId, message).toJsonString()
            is NOTICE -> listOf("NOTICE", message).toJsonString()
        }
    }

    fun toClient(session: WebSocketSession) {
        if (session.isOpen) {
            val payload = this.toJson()
            try {
                session.sendSync(payload)
                if (this is CLOSED) {
                    session.close()
                }
            } catch (e: Exception) {
                LOG.error("Error sending WebSocket message: ${e.message}")
            }
        } else {
            LOG.warn("Attempted to send message to closed WebSocket session.")
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(RelayResponse::class.java)
    }

}