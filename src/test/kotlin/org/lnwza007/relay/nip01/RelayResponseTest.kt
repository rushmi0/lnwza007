package org.lnwza007.relay.nip01

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.lnwza007.relay.modules.Event
import org.mockito.Mockito.*
import org.mockito.ArgumentCaptor
import io.micronaut.websocket.WebSocketSession
import org.lnwza007.relay.service.nip01.response.RelayResponse
import org.lnwza007.util.ShiftTo.toJsonString

class RelayResponseTest {

    private val event = Event(
        id = "000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358",
        pubkey = "a48380f4cfcc1ad5378294fcac36439770f9c878dd880ffa94bb74ea54a6f243",
        createAt = 1651794653,
        kind = 1,
        tags = listOf(listOf("nonce", "776797", "20")),
        content = "It's just me mining my own business",
        signature = "284622fc0a3f4f1303455d5175f7ba962a3300d136085b9566801bc2e0699de0c7e31e44c81fb40ad9049173742e904713c3594a1da0fc5d2382a25c11aba977"
    )

    @Test
    fun `test EVENT to Json String`() {
        val relayResponse = RelayResponse.EVENT("b1a649ebe8", event)
        val expectedJson = """["EVENT","b1a649ebe8",${event.toJsonString()}]"""
        assertEquals(expectedJson, relayResponse.toJson())
    }

    @Test
    fun `test OK to Json String`() {
        val relayResponse = RelayResponse.OK("000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358", true, "")
        val expectedJson = """["OK","000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358",true,""]"""
        assertEquals(expectedJson, relayResponse.toJson())
    }

    @Test
    fun `test OK toJson with message`() {
        val relayResponse = RelayResponse.OK("000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358", false, "duplicate: already have this event")
        val expectedJson = """["OK","000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358",false,"duplicate: already have this event"]"""
        assertEquals(expectedJson, relayResponse.toJson())
    }

    @Test
    fun `test EOSE to Json String`() {
        val relayResponse = RelayResponse.EOSE("b1a649ebe8")
        val expectedJson = """["EOSE","b1a649ebe8"]"""
        assertEquals(expectedJson, relayResponse.toJson())
    }

    @Test
    fun `test NOTICE to Json String`() {
        val relayResponse = RelayResponse.NOTICE("unrecognised filter item")
        val expectedJson = """["NOTICE","unrecognised filter item"]"""
        assertEquals(expectedJson, relayResponse.toJson())
    }

    @Test
    fun `test CLOSED to Json String`() {
        val relayResponse = RelayResponse.CLOSED("b1a649ebe8", "unsupported: filter contains unknown fields")
        val expectedJson = """["CLOSED","b1a649ebe8","unsupported: filter contains unknown fields"]"""
        assertEquals(expectedJson, relayResponse.toJson())
    }

    @Test
    fun `test EVENT send to Client`() {
        val session = mock(WebSocketSession::class.java)
        `when`(session.isOpen).thenReturn(true)

        val relayResponse = RelayResponse.EVENT("b1a649ebe8", event)
        val expectedJson = """["EVENT","b1a649ebe8",${event.toJsonString()}]"""

        relayResponse.toClient(session)

        val captor = ArgumentCaptor.forClass(String::class.java)
        verify(session).sendSync(captor.capture())
        assertEquals(expectedJson, captor.value)
    }

}
