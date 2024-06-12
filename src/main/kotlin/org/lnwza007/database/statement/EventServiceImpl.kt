package org.lnwza007.database.statement


import io.reactivex.rxjava3.core.Single
import jakarta.inject.Inject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nostr.relay.infra.database.tables.Event.EVENT
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.lnwza007.database.service.EventService
import org.lnwza007.relay.modules.Event
import org.lnwza007.util.CoroutineManager.parallelIO
import org.slf4j.LoggerFactory


class EventServiceImpl @Inject constructor(private val enforce: DSLContext) : EventService {


    override suspend fun saveEvent(event: Event): Boolean {
        return parallelIO(100) {
            Single.fromCallable {

                /**
                 * INSERT INTO EVENT
                 * (
                 *      event_id,
                 *      pubkey,
                 *      created_at,
                 *      kind,
                 *      tags,
                 *      content,
                 *      sig
                 * )
                 *
                 * VALUES
                 * (
                 *      <eventId>,
                 *      <pubkey>,
                 *      <createdAt>,
                 *      <kind>,
                 *      <tags>,
                 *      <content>,
                 *      <sig>
                 * )
                 */

                enforce.insertInto(
                    EVENT,
                    EVENT.EVENT_ID,
                    EVENT.PUBKEY,
                    EVENT.CREATED_AT,
                    EVENT.KIND,
                    EVENT.TAGS,
                    EVENT.CONTENT,
                    EVENT.SIG
                ).values(
                    DSL.`val`(event.id).cast(SQLDataType.VARCHAR.length(64)),
                    DSL.`val`(event.pubkey).cast(SQLDataType.VARCHAR.length(64)),
                    DSL.`val`(event.createAt).cast(SQLDataType.INTEGER),
                    DSL.`val`(event.kind).cast(SQLDataType.INTEGER),
                    DSL.`val`(Json.encodeToString(event.tags)).cast(SQLDataType.JSONB),
                    DSL.`val`(event.content).cast(SQLDataType.CLOB),
                    DSL.`val`(event.signature).cast(SQLDataType.VARCHAR.length(128))
                ).execute() > 0
            }
                .doOnSuccess { result ->
                    LOG.info("Event saved: ${if (result) event.id else "Failed"}")
                }
                .doOnError { e ->
                    LOG.error("Error saving event: ${e.message}")
                }
                .blockingGet()
        }
    }


    override suspend fun deleteEvent(eventId: String): Boolean {
        return parallelIO {
            Single.fromCallable {

                /**
                 * DELETE
                 * FROM event
                 * WHERE event_id = <eventId>;
                 */

                val deletedCount = enforce.deleteFrom(EVENT)
                    .where(EVENT.EVENT_ID.eq(DSL.`val`(eventId)))
                    .execute()

                deletedCount > 0
            }
                .doOnSuccess { result ->
                    LOG.info("Event deleted: ${if (result) eventId else "Failed"}")
                }
                .doOnError { e ->
                    LOG.error("Error deleting event: ${e.message}")
                }
                .blockingGet()
        }
    }


    override suspend fun selectAll(): List<Event> {
        return parallelIO {
            Single.fromCallable {

                /**
                 * SELECT *
                 * FROM event;
                 */

                val result = enforce.select().from(EVENT)
                //LOG.info("${result.fetch()}")

                result.fetch()
                    .map { record ->
                        Event(
                            id = record[EVENT.EVENT_ID],
                            pubkey = record[EVENT.PUBKEY],
                            createAt = record[EVENT.CREATED_AT].toLong(),
                            kind = record[EVENT.KIND].toLong(),
                            tags = Json.decodeFromString(record[EVENT.TAGS].toString()),
                            content = record[EVENT.CONTENT],
                            signature = record[EVENT.SIG]
                        )
                    }
            }
                .doOnSuccess { events ->
                    LOG.info("Events retrieved: ${events.size}")
                }
                .doOnError { e ->
                    LOG.error("Error retrieving events: ${e.message}")
                }
                .blockingGet()
        }
    }


    companion object {
        private val LOG = LoggerFactory.getLogger(EventServiceImpl::class.java)
    }

}
