package org.lnwza007.relay.service.nip01

import io.micronaut.websocket.WebSocketSession
import jakarta.inject.Inject
import org.lnwza007.database.statement.EventServiceImpl
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.FiltersX
import org.lnwza007.relay.service.nip01.response.RelayResponse
import org.lnwza007.relay.service.nip09.EventDeletion
import org.lnwza007.relay.service.nip13.ProofOfWork
import org.lnwza007.util.CoroutineManager.parallelIO
import org.slf4j.LoggerFactory

class BasicProtocolFlow @Inject constructor(
    private val service: EventServiceImpl,
//    private val nip09: EventDeletion,
//    private val nip13: ProofOfWork
) {

    suspend fun onEvent(event: Event, status: Boolean, warning: String, session: WebSocketSession) {
        LOG.info("Received event: $event")

        parallelIO(300) {
            if (status) {
                val existingEvent: Event? = service.selectById(event.id!!)
                if (existingEvent == null) {
                    // ไม่พบข้อมูลในฐานข้อมูล ดำเนินการบันทึกข้อมูล
                    val result: Boolean = service.saveEvent(event)
                    LOG.info("Event saved status: $result")
                    RelayResponse.OK(eventId = event.id, isSuccess = result, message = warning).toClient(session)
                } else {
                    // พบข้อมูลในฐานข้อมูลแล้ว ส่งข้อมูลเป็นค่าซ้ำกลับไปยัง client
                    LOG.info("Event with ID ${event.id} already exists in the database.")
                    RelayResponse.OK(eventId = event.id, isSuccess = false, message = "Duplicate: already have this event").toClient(session)
                }

            } else {
                RelayResponse.OK(eventId = event.id!!, isSuccess = false, message = warning).toClient(session)
            }
        }

    }


    suspend fun onRequest(
        subscriptionId: String,
        filtersX: List<FiltersX>,
        status: Boolean,
        warning: String,
        session: WebSocketSession
    ) {
        if (status) {
            //LOG.info("request for subscription ID: $subscriptionId with filters: $filtersX")

            for (filter in filtersX) {
                val events = service.filterList(filter)
                events.forEach { event ->
                    RelayResponse.EVENT(subscriptionId, event).toClient(session)
                }
            }
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


    private fun processEvent() {

    }

    private val LOG = LoggerFactory.getLogger(BasicProtocolFlow::class.java)
}
