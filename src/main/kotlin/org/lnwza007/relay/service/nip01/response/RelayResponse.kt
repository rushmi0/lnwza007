package org.lnwza007.relay.service.nip01.response

import io.micronaut.websocket.WebSocketSession
import org.lnwza007.relay.modules.Event
import org.lnwza007.util.ShiftTo.toJsonString

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
        val jsonString = this.toJson()
        session.sendSync(jsonString)

        if (this is CLOSED) {
            session.close()
        }
    }

}