package org.lnwza007.relay.database

import kotlinx.coroutines.runBlocking
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.*

import org.lnwza007.database.statement.EventServiceImpl
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.service.nip01.Transform.toEvent
import org.lnwza007.util.ShiftTo.toJsonElementMap

import java.sql.Connection
import java.sql.DriverManager

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class EventServiceTest {

    private lateinit var connection: Connection
    private lateinit var dslContext: DSLContext

    companion object {
        private const val JDBC_URL = "jdbc:postgresql://localhost:54330/nostr"
        private const val USERNAME = "rushmi0"
        private const val PASSWORD = "0sql@min1"

        private val data = """
            {
                "kind":0,
                "created_at":1716448321,
                "tags":[["alt","User profile for lnwza007"]],
                "content":"{\"name\":\"lnwza007\",\"gender\":\"\",\"area\":\"\",\"picture\":\"https://image.nostr.build/552b5424ebd3c66be6f588e08c2f427e04423f11e80514414215b5ae00877b28.gif\",\"lud16\":\"rushmi0@getalby.com\",\"website\":\"https://github.com/rushmi0\",\"display_name\":\"lnwza007\",\"banner\":\"\",\"about\":\"แดดกรุงเทพที่ร้อนจ้า ยังแพ้ตัวข้าที่ร้อน sat\"}",
                "pubkey":"e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3",
                "id":"ecfdf5d329ae69bdca40f04a33a8f8447b83824f958a8db926430cd8b2aeb350",
                "sig":"6a7898997ceb936fb6f660848baf8185f84ab22ff45aa3fc36eabad577bb4fae739bfdcd3d428d52146c6feaf9264bbc8f82121ddb8eeb85ce242ff79a1b0948"
            }
        """.trimIndent()

        private val event: Event = data.toJsonElementMap().toEvent()
    }

    @BeforeAll
    fun setUp() {
        connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD)
        dslContext = DSL.using(connection, SQLDialect.POSTGRES)
    }

    @AfterAll
    fun tearDown() {
        if (!connection.isClosed) {
            connection.close()
        }
    }


    @Test
    fun `test Save Event data to database`() = runBlocking {
        val eventService = EventServiceImpl(dslContext)
        val result: Boolean = eventService.saveEvent(event)
        Assertions.assertTrue(result)
    }


    @Test
    fun `test Delete Event from database`() = runBlocking {
        val eventService = EventServiceImpl(dslContext)
        val result: Boolean = eventService.deleteEvent(event.id!!)
        Assertions.assertTrue(result)
    }



}
