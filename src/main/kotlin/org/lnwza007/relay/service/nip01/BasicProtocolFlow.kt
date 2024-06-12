package org.lnwza007.relay.service.nip01

import io.micronaut.websocket.WebSocketSession
import jakarta.inject.Inject
import org.lnwza007.database.statement.EventServiceImpl
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.FiltersX
import org.lnwza007.relay.service.nip01.response.RelayResponse
import org.lnwza007.relay.service.nip09.EventDeletion
import org.slf4j.LoggerFactory

class BasicProtocolFlow @Inject constructor(
    private val service: EventServiceImpl,
    private val nip09: EventDeletion,
    //    private val nip13: ProofOfWork
) {

    suspend fun onEvent(event: Event, status: Boolean, warning: String, session: WebSocketSession) {
        val result = service.saveEvent(event)
        LOG.info("Saved event status: $result")
        LOG.info("event: $event")
        RelayResponse.OK(eventId = event.id!!, isSuccess = status, message = warning).toClient(session)
    }

    suspend fun onRequest(
        subscriptionId: String,
        filtersX: List<FiltersX>,
        status: Boolean,
        warning: String,
        session: WebSocketSession
    ) {
        if (status) {
            LOG.info("request for subscription ID: $subscriptionId with filters: $filtersX")
            RelayResponse.EOSE(subscriptionId = subscriptionId).toClient(session)
        } else {
            RelayResponse.NOTICE(warning).toClient(session)
        }
    }

    fun onClose(subscriptionId: String, session: WebSocketSession) {
        LOG.info("close request for subscription ID: $subscriptionId")
        RelayResponse.CLOSED(subscriptionId = subscriptionId, message = "").toClient(session)
    }

    fun onUnknown(session: WebSocketSession) {
        LOG.warn("Unknown command")
        RelayResponse.NOTICE("Unknown command").toClient(session)
    }

    private val LOG = LoggerFactory.getLogger(BasicProtocolFlow::class.java)
}
