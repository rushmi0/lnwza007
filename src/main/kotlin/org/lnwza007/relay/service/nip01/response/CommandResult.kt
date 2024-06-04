package org.lnwza007.relay.service.nip01.response

import io.micronaut.websocket.WebSocketSession
import org.lnwza007.relay.modules.Event

data class CommandResult(
    val event: Event,
    val message: String
) {
    companion object {

    }

    fun respond(session: WebSocketSession) {
        session.sendSync("")
    }
}

/*
data class CommandResult(val eventId: String, val result: Boolean, val description: String = "") {
    fun toJson(): String {
        return jacksonObjectMapper().writeValueAsString(
            listOf("OK", eventId, result, description)
        )
    }

    companion object {
        fun ok(event: Event) = CommandResult(event.id, true)
        fun duplicated(event: Event) = CommandResult(event.id, true, "duplicate:")
        fun invalid(event: Event, message: String) = CommandResult(event.id, false, "invalid: $message")
        fun error(event: Event, message: String) = CommandResult(event.id, false, "error: $message")
    }
}
*/