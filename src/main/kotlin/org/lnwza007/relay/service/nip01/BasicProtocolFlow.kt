package org.lnwza007.relay.service.nip01

import io.micronaut.websocket.WebSocketSession
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import org.lnwza007.relay.service.nip01.Transform.toEvent
import org.lnwza007.relay.service.nip01.Transform.toFiltersX
import org.lnwza007.relay.service.nip09.EventDeletion
import org.lnwza007.relay.service.nip13.ProofOfWork
import org.lnwza007.util.ShiftTo.toJsonElementMap
import org.slf4j.LoggerFactory

class BasicProtocolFlow @Inject constructor(
//    private val nip09: EventDeletion,
//    private val nip13: ProofOfWork
) {


    suspend fun onEvent(payload: String, session: WebSocketSession) {

        /*
        val (_, message, event) = mag.toJsonElementMap().toEvent().first()

        event?.let {
            LOG.info("Event: $event")
            LOG.info("Event Message: $message")
            LOG.info("session: $session")
        }
         */
    }

    suspend fun onRequest(payload: String, subscriptionId: String, session: WebSocketSession) {

        /*
        val (_, message, filter) = mag.toJsonElementMap().toFiltersX().first()

        filter?.let {
            LOG.info("Filter: $filter")
            LOG.info("FilterX Message: $message")
            LOG.info("Subscription ID: $subscriptionId session: $session")
        }
         */

    }

    fun onClose(session: WebSocketSession) {
        session.close()
    }

    fun onUnknown(session: WebSocketSession) {
        session.sendSync("")
        session.close()
    }

    private val LOG = LoggerFactory.getLogger(BasicProtocolFlow::class.java)

}
