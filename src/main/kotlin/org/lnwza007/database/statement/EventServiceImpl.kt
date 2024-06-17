package org.lnwza007.database.statement


import io.reactivex.rxjava3.core.Single
import jakarta.inject.Inject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import nostr.relay.infra.database.tables.Event.EVENT
import nostr.relay.infra.database.tables.records.EventRecord
import org.jooq.DSLContext
import org.jooq.SelectWhereStep
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
                    DSL.`val`(event.pubkey).cast(SQLDataType.VARCHAR.length(64)),
                    DSL.`val`(event.created_at).cast(SQLDataType.INTEGER),
                    DSL.`val`(event.kind).cast(SQLDataType.INTEGER),
                    DSL.`val`(Json.encodeToString(event.tags)).cast(SQLDataType.JSONB),
                    DSL.`val`(event.content).cast(SQLDataType.CLOB),
                    DSL.`val`(event.sig).cast(SQLDataType.VARCHAR.length(128))
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
            LOG.info("\n$record")
            if (record != null) {
                Event(
                    id = record[EVENT.EVENT_ID],
                    pubkey = record[EVENT.PUBKEY],
                    created_at = record[EVENT.CREATED_AT].toLong(),
                    kind = record[EVENT.KIND].toLong(),
                    tags = Json.decodeFromString<List<List<String>>>(record[EVENT.TAGS].toString()),
                    content = record[EVENT.CONTENT],
                    sig = record[EVENT.SIG]
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
                 * สร้างคำสั่ง SQL สำหรับการดึงข้อมูลจากตาราง EVENT โดยพิจารณาจากตัวกรองที่ได้รับ
                 * SELECT * FROM EVENT
                 * WHERE
                 *   EVENT.EVENT_ID IN (:ids)
                 *   AND EVENT.PUBKEY IN (:authors)
                 *   AND EVENT.KIND IN (:kinds)
                 *   AND (jsonb_field ->> 'key' IN (:values) AND jsonb_field ->> 'key2' IN (:values2) ...)
                 *   AND EVENT.CREATED_AT >= :since
                 *   AND EVENT.CREATED_AT <= :until
                 *   AND to_tsvector('simple', EVENT.CONTENT) @@ plainto_tsquery('simple', :search)
                 * LIMIT :limit
                 */

                // สร้างคำสั่ง SELECT ขึ้นมาโดยเลือกข้อมูลทั้งหมดจากตาราง EVENT
                val query: SelectWhereStep<EventRecord> = enforceSQL.selectFrom(EVENT)

                // ถ้ามีการระบุ ids ใน filters ให้เพิ่มเงื่อนไขการค้นหา EVENT_ID ใน ids ที่กำหนด
                filters.ids.takeIf { it.isNotEmpty() }?.let { query.where(EVENT.EVENT_ID.`in`(it)) }

                // ถ้ามีการระบุ authors ใน filters ให้เพิ่มเงื่อนไขการค้นหา PUBKEY ใน authors ที่กำหนด
                filters.authors.takeIf { it.isNotEmpty() }?.let { query.where(EVENT.PUBKEY.`in`(it)) }

                // ถ้ามีการระบุ kinds ใน filters ให้เพิ่มเงื่อนไขการค้นหา KIND ใน kinds ที่กำหนด
                filters.kinds.takeIf { it.isNotEmpty() }?.let { query.where(EVENT.KIND.`in`(it)) }

                // ถ้ามีการระบุ tags ใน filters ให้เพิ่มเงื่อนไขการค้นหา TAGS ที่ตรงกับค่าที่กำหนด
                filters.tags.forEach { (key, values) ->
                    values.forEach { value ->
                        /**
                         * สร้างเงื่อนไขการค้นหาสำหรับฟิลด์ TAGS ที่เป็นประเภท JSONB
                         * โดยใช้ JSONB containment operator (@>) ใน PostgreSQL
                         *
                         * jsonValue ใช้ DSL.field เพื่อสร้างเงื่อนไขการค้นหา โดยใช้โครงสร้าง {0} @> {1}::jsonb
                         * - {0} คือฟิลด์ TAGS ในตาราง EVENT
                         * - {1} คือค่า JSON ที่ต้องการเช็คการครอบคลุม
                         *
                         * ตัวอย่าง JSON ที่ใช้เช็ค: [["key","value"]]
                         * DSL.inline ใช้เพื่อแปลงสตริงเป็นประเภท JSONB ใน SQL
                         */
                        val jsonValue = DSL.field(
                            "{0} @> {1}::jsonb",
                            Boolean::class.java,
                            EVENT.TAGS,
                            DSL.inline("""[["${key.tag}","$value"]]""", SQLDataType.JSONB)
                        )
                        query.where(jsonValue)
                    }
                }

                // ถ้ามีการระบุ since ใน filters ให้เพิ่มเงื่อนไขการค้นหา CREATED_AT ที่มีค่ามากกว่าหรือเท่ากับ since ที่กำหนด
                filters.since?.let { query.where(EVENT.CREATED_AT.greaterOrEqual(it.toInt())) }

                // ถ้ามีการระบุ until ใน filters ให้เพิ่มเงื่อนไขการค้นหา CREATED_AT ที่มีค่าน้อยกว่าหรือเท่ากับ until ที่กำหนด
                filters.until?.let { query.where(EVENT.CREATED_AT.lessOrEqual(it.toInt())) }

                // ถ้ามีการระบุ search ใน filters ให้เพิ่มเงื่อนไขการค้นหา CONTENT ที่ตรงกับ search ที่กำหนดโดยใช้ full-text search
                filters.search?.let {
                    /**
                     * สร้างเงื่อนไขการค้นหาสำหรับฟิลด์ CONTENT โดยใช้ full-text search ใน PostgreSQL
                     *
                     * textSearchQuery ใช้ DSL.field เพื่อสร้างเงื่อนไขการค้นหา โดยใช้โครงสร้าง to_tsvector('simple', {0}) @@ plainto_tsquery('simple', {1})
                     * - {0} คือฟิลด์ CONTENT ในตาราง EVENT
                     * - {1} คือข้อความที่ต้องการค้นหา
                     *
                     * - to_tsvector ใช้แปลงข้อความในฟิลด์ CONTENT เป็นเวกเตอร์ของคำ
                     * - plainto_tsquery ใช้แปลงข้อความค้นหาเป็น text search query object
                     * - @@ เป็น operator ที่ใช้เช็คว่า text search query object นั้นๆ ตรงกันกับ to_tsvector หรือไม่
                     */
                    val textSearchQuery = DSL.field(
                        "to_tsvector('simple', {0}) @@ plainto_tsquery('simple', {1})",
                        Boolean::class.java,
                        EVENT.CONTENT,
                        DSL.inline(it)
                    )
                    query.where(textSearchQuery)
                }

                // กำหนด limit ของการดึงข้อมูล ถ้า filters.limit เป็น null ให้ใช้ค่าเริ่มต้นเป็น 1,700 record
                query.limit(filters.limit?.toInt() ?: 1_700)

                // ดำเนินการ fetch ข้อมูลตามเงื่อนไขที่กำหนดแล้ว map ข้อมูลที่ได้มาเป็น Event objects
                query.fetch().map { record ->
                    Event(
                        id = record[EVENT.EVENT_ID],
                        pubkey = record[EVENT.PUBKEY],
                        created_at = record[EVENT.CREATED_AT].toLong(),
                        kind = record[EVENT.KIND].toLong(),
                        tags = Json.decodeFromString(record[EVENT.TAGS].toString()),
                        content = record[EVENT.CONTENT],
                        sig = record[EVENT.SIG]
                    )
                }
            }.blockingGet()
        }
    }



    companion object {
        private val LOG = LoggerFactory.getLogger(EventServiceImpl::class.java)
    }

}
