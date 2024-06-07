package org.lnwza007.relay.nip01

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.lnwza007.relay.service.nip01.CLOSE
import org.lnwza007.relay.service.nip01.EVENT
import org.lnwza007.relay.service.nip01.REQ
import org.lnwza007.relay.service.nip01.parseCommand
import java.lang.IllegalArgumentException

class CommandParserTest {

    @Test
    fun `parse valid EVENT command`() {
        val json = """
            [
                "EVENT",
                {
                    "id": "000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358",
                    "pubkey": "a48380f4cfcc1ad5378294fcac36439770f9c878dd880ffa94bb74ea54a6f243",
                    "created_at": 1651794653,
                    "kind": 1,
                    "tags": [["nonce", "776797", "20"]],
                    "content": "It's just me mining my own business",
                    "sig": "284622fc0a3f4f1303455d5175f7ba962a3300d136085b9566801bc2e0699de0c7e31e44c81fb40ad9049173742e904713c3594a1da0fc5d2382a25c11aba977"
                }
            ]
        """.trimIndent()

        val command = parseCommand(json) as EVENT
        val event = command.event

        // เพิ่ม assertion ตรวจสอบว่า event ถูกสร้างด้วยข้อมูลที่ถูกต้อง
        assert(event.id == "000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358")
        assert(event.pubkey == "a48380f4cfcc1ad5378294fcac36439770f9c878dd880ffa94bb74ea54a6f243")
        assert(event.createAt?.toInt() == 1651794653)
        assert(event.kind?.toInt() == 1)
        assert(event.tags == listOf(listOf("nonce", "776797", "20")))
        assert(event.content == "It's just me mining my own business")
        assert(event.signature == "284622fc0a3f4f1303455d5175f7ba962a3300d136085b9566801bc2e0699de0c7e31e44c81fb40ad9049173742e904713c3594a1da0fc5d2382a25c11aba977")
    }

    @Test
    fun `parse valid REQ command`() {
        val json = """
            [
                "REQ", 
                "ffff", 
                {"search": "purple", "kinds": [1], "since": 1715181359}, 
                {"kinds": [1], "authors": ["161498ed3277aa583c301288de5aafda4f317d2bf1ad0a880198a9dede37a6aa"]}
            ] 
        """.trimIndent()

        val command = parseCommand(json) as REQ
        val subscriptionId = command.subscriptionId
        val filters = command.filtersX

        assert(subscriptionId == "ffff")
        assert(filters.size == 2)
        assert(filters[0].search == "purple")
        filters[0].kinds?.let { assert(it.containsAll(listOf(1))) }
        assert(filters[0].since?.toInt() == 1715181359)
        filters[1].kinds?.let { assert(it.containsAll(listOf(1))) }
        filters[1].authors?.let {
            assert(
                it.containsAll(
                    listOf("161498ed3277aa583c301288de5aafda4f317d2bf1ad0a880198a9dede37a6aa")
                )
            )
        }
    }

    @Test
    fun `parse valid CLOSE command`() {
        val json = """["CLOSE", "ffff"]"""

        val command = parseCommand(json) as CLOSE
        val subscriptionId = command.subscriptionId

        assert(subscriptionId == "ffff")
    }

    @Test
    fun `parse invalid command format`() {
        val json = """[]"""

        val exception = assertThrows<IllegalArgumentException> {
            parseCommand(json)
        }
        assert(exception.message == "Invalid command format")
    }

    @Test
    fun `parse invalid JSON format`() {
        val json = "invalid json"

        val exception = assertThrows<IllegalArgumentException> {
            parseCommand(json)
        }
        assert(exception.message == "Invalid JSON format")
    }
}
