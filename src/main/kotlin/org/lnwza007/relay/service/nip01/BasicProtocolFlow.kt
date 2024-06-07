package org.lnwza007.relay.service.nip01

import io.micronaut.websocket.WebSocketSession
import jakarta.inject.Inject
import org.slf4j.LoggerFactory

class BasicProtocolFlow @Inject constructor(
//    private val nip09: EventDeletion,
//    private val nip13: ProofOfWork
) {


    suspend fun onEvent(payload: Command, session: WebSocketSession) {

    }

    suspend fun onRequest(payload: Command, subscriptionId: String, session: WebSocketSession) {

    }

    fun onClose(session: WebSocketSession) {

    }

    fun onUnknown(session: WebSocketSession) {
        session.sendSync("")
        session.close()
    }

    private val LOG = LoggerFactory.getLogger(BasicProtocolFlow::class.java)

}
