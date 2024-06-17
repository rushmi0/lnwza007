package org.lnwza007.relay.service.nip01

import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.service.nip01.Transform.toJsonString
import org.lnwza007.util.Schnorr
import org.lnwza007.util.ShiftTo.generateId

data class VerifyEvent(val receive: Event) {

    fun isValidEventId(): Pair<Boolean, String> {
        val actualId = generateId(receive)
        return if (receive.id != actualId) {
            Pair(false, "Invalid: actual event id $actualId")
        } else {
            Pair(true, "")
        }
    }

    fun isValidSignature(): Pair<Boolean, String> {
        val eventId = if (isValidEventId().first) receive.id!! else generateId(receive)
        if (!Schnorr.verify(eventId, receive.pubkey!!, receive.sig!!)) {
            val warning = """
                |Invalid: bad signature
                |  Event: ${receive.toJsonString()}
                |  Actual: ${generateId(receive)}
            """.trimIndent()
            return Pair(false, warning)
        }
        return Pair(true, "")
    }

}
