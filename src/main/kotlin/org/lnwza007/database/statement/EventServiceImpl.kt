package org.lnwza007.database.statement


import io.reactivex.rxjava3.core.Single
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nostr.relay.infra.database.tables.Event.EVENT
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.lnwza007.database.service.EventService
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.FiltersX
import org.lnwza007.util.CoroutineManager.parallelIO
import org.slf4j.LoggerFactory


class EventServiceImpl @Inject constructor(private val enforceSQL: DSLContext) : EventService {


    override suspend fun saveEvent(event: Event): Boolean {
        return parallelIO(64) {
            Single.fromCallable {

                /**
                 * INSERT INTO EVENT
                 * (event_id, pubkey, created_at, kind, tags, content, sig)
                 * VALUES
                 * (:eventId, :pubkey, :createdAt, :kind, :tags, :content, :sig)
                 */

                enforceSQL.insertInto(
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
                    DSL.`val`(event.pubKey).cast(SQLDataType.VARCHAR.length(64)),
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
        return parallelIO(64) {
            Single.fromCallable {

                /**
                 * DELETE
                 * FROM event
                 * WHERE event_id = :eventId;
                 */

                val deletedCount = enforceSQL.deleteFrom(EVENT)
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



    override suspend fun selectById(id: String): Event? {
        return parallelIO(64) {

            /**
             * SELECT * FROM event
             * WHERE event_id = :id
             */

            val record = enforceSQL.selectFrom(EVENT)
                .where(EVENT.EVENT_ID.eq(DSL.`val`(id)))
                .fetchOne()

            if (record != null) {
                Event(
                    id = record[EVENT.EVENT_ID],
                    pubKey = record[EVENT.PUBKEY],
                    createAt = record[EVENT.CREATED_AT].toLong(),
                    kind = record[EVENT.KIND].toLong(),
                    tags = Json.decodeFromString<List<List<String>>>(record[EVENT.TAGS].toString()),
                    content = record[EVENT.CONTENT],
                    signature = record[EVENT.SIG]
                )
            } else {
                LOG.info("Event not found for ID: $id")
                null
            }
        }
    }


    override suspend fun filterList(filters: FiltersX): List<Event> {
        return parallelIO(100) {
            Single.fromCallable {

                /**
                 * SELECT * FROM EVENT
                 * WHERE EVENT.EVENT_ID IN (:ids)
                 *   AND EVENT.PUBKEY IN (:authors)
                 *   AND EVENT.KIND IN (:kinds)
                 *   AND (EVENT.TAGS @> '[["key1","value1"]]') OR (EVENT.TAGS @> '[["key2","value2"]]') OR ...
                 *   AND EVENT.CREATED_AT >= :since
                 *   AND EVENT.CREATED_AT <= :until
                 *   AND EVENT.CONTENT LIKE :search
                 *   LIMIT :limit
                 */

                val query = enforceSQL.selectFrom(EVENT)

                filters.ids.takeIf { it.isNotEmpty() }?.let { query.where(EVENT.EVENT_ID.`in`(it)) }
                filters.authors.takeIf { it.isNotEmpty() }?.let { query.where(EVENT.PUBKEY.`in`(it)) }
                filters.kinds.takeIf { it.isNotEmpty() }?.let { query.where(EVENT.KIND.`in`(it)) }
                filters.tags.forEach { (key, values) ->
                    // Use @> operator for JSONB containment check
                    values.forEach { value ->
                        val sqlString = "{0} @> {1}::jsonb"
                        val jsonValue = DSL.field(sqlString, Boolean::class.java, EVENT.TAGS, DSL.inline("""[["${key.tag}","$value"]]""", SQLDataType.JSONB))
                        query.where(jsonValue)
                    }
                }
                filters.since?.let { query.where(EVENT.CREATED_AT.greaterOrEqual(it.toInt())) }
                filters.until?.let { query.where(EVENT.CREATED_AT.lessOrEqual(it.toInt())) }
                filters.search?.let { query.where(EVENT.CONTENT.contains(it)) }
                filters.limit?.let { query.limit(it.toInt()) }

                LOG.info("$query")
                query.fetch().map { record ->
                    Event(
                        id = record[EVENT.EVENT_ID],
                        pubKey = record[EVENT.PUBKEY],
                        createAt = record[EVENT.CREATED_AT].toLong(),
                        kind = record[EVENT.KIND].toLong(),
                        tags = Json.decodeFromString(record[EVENT.TAGS].toString()),
                        content = record[EVENT.CONTENT],
                        signature = record[EVENT.SIG]
                    )
                }
            }.blockingGet()
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(EventServiceImpl::class.java)
    }

}
