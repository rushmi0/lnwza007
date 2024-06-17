package org.lnwza007.relay.service.nip01

import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.service.nip01.Transform.toJsonString
import org.lnwza007.util.Schnorr
import org.lnwza007.util.ShiftTo.generateId

object VerifyEvent {

    fun Event.isValidEventId(): Pair<Boolean, String> {
        val actualId = generateId(this)
        return if (this.id != actualId) {
            Pair(false, "Invalid: actual event id $actualId")
        } else {
            Pair(true, "")
        }
    }

    fun Event.isValidSignature(): Pair<Boolean, String> {
        val eventId = if (isValidEventId().first) this.id!! else generateId(this)
        if (!Schnorr.verify(eventId, this.pubkey!!, this.sig!!)) {
            val warning = """
                |Invalid: bad signature
                |  Event: ${this.toJsonString()}
                |  Actual: ${generateId(this)}
            """.trimIndent()
            return Pair(false, warning)
        }
        return Pair(true, "")
    }

}
