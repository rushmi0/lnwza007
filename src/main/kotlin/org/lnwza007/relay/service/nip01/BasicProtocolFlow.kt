package org.lnwza007.relay.service.nip01

import io.micronaut.websocket.WebSocketSession
import jakarta.inject.Inject
import org.lnwza007.relay.service.nip09.EventDeletion
import org.lnwza007.relay.service.nip13.ProofOfWork


class BasicProtocolFlow @Inject constructor(
    private val nip09: EventDeletion,
    private val nip13: ProofOfWork
) {



    fun onNewEvent(session: WebSocketSession) {

    }


    fun onRequest(session: WebSocketSession) {

    }


}
