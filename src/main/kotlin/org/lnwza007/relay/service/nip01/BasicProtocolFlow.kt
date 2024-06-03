package org.lnwza007.relay.service.nip01

import io.micronaut.websocket.WebSocketSession
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.FiltersX
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


    suspend fun onEvent(mag: String, session: WebSocketSession) {

        val event = mag.toJsonElementMap().toEvent()
        LOG.info("Event: ${event.first().second}")
        LOG.info("session: $session")
    }

    suspend fun onRequest(mag: String, subscriptionId: String, session: WebSocketSession) {
        val filter = mag.toJsonElementMap().toFiltersX()
        LOG.info("Filter: ${filter.first().second}")
        LOG.info("Subscription ID: $subscriptionId session: $session")
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
