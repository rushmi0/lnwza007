package org.lnwza007.relay.nip01

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.lnwza007.relay.modules.FiltersX
import org.lnwza007.relay.modules.TAG_D
import org.lnwza007.relay.modules.TAG_P
import org.lnwza007.relay.service.nip01.command.CommandProcessor.parse
import org.lnwza007.relay.service.nip01.command.CLOSE
import org.lnwza007.relay.service.nip01.command.EVENT
import org.lnwza007.relay.service.nip01.command.REQ
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

        val (command, _) = parse(json)
        command as EVENT
        val event = command.event

        assertEquals(event.id, "000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358")
        assertEquals(event.pubkey, "a48380f4cfcc1ad5378294fcac36439770f9c878dd880ffa94bb74ea54a6f243")
        assertEquals(event.created_at?.toInt(), 1651794653)
        assertEquals(event.kind?.toInt(), 1)
        assertEquals(event.tags, listOf(listOf("nonce", "776797", "20")))
        assertEquals(event.content, "It's just me mining my own business")
        assertEquals(
            event.sig,
            "284622fc0a3f4f1303455d5175f7ba962a3300d136085b9566801bc2e0699de0c7e31e44c81fb40ad9049173742e904713c3594a1da0fc5d2382a25c11aba977"
        )
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

        val (command, _) = parse(json)
        command as REQ

        val subscriptionId = command.subscriptionId
        val filters = command.filtersX

        assertEquals(subscriptionId, "ffff")
        assertEquals(filters.size, 2)
        assertEquals(filters[0].search, "purple")

        assert(filters[0].kinds.containsAll(listOf(1)))
        assertEquals(filters[0].since?.toInt(), 1715181359)
        assert(filters[1].kinds.containsAll(listOf(1)))
        assert(
            filters[1].authors.containsAll(
                listOf("161498ed3277aa583c301288de5aafda4f317d2bf1ad0a880198a9dede37a6aa")
            )
        )
    }

    @Test
    fun `parse valid CLOSE command`() {
        val json = """["CLOSE", "ffff"]"""

        val (command, _) = parse(json)
        command as CLOSE
        val subscriptionId = command.subscriptionId

        assertEquals(subscriptionId, "ffff")
    }

    @Test
    fun `parse invalid command format`() {
        val json = """[]"""

        val exception = assertThrows<IllegalArgumentException> {
            parse(json)
        }

        assertEquals(
            exception.message,
            "Invalid: command format"
        )
    }

    @Test
    fun `parse invalid JSON format`() {
        val json = "invalid json"

        val exception = assertThrows<IllegalArgumentException> {
            parse(json)
        }
        assertEquals(exception.message, "Invalid: JSON format")
    }


    @Test
    fun `parse valid complex REQ command`() {
        val json = """
        [
            "REQ",
            "8wHEWFsnIvKCWTb-4PMak",
            {
                "#d":[
                    "3425e3a156471426798b80c1da1f148343c5c5b4d2ac452d3330a91b4619af65",
                    "3425e3a156471426798b80c1da1f148343c5c5b4d2ac452d3330a91b4619af65",
                    "161498ed3277aa583c301288de5aafda4f317d2bf1ad0a880198a9dede37a6aa"
                ],
                "kinds":[1,6,16,7,9735,2004,30023],
                "limit":50
            },
            {
                "kinds": [4],
                "#p": ["161498ed3277aa583c301288de5aafda4f317d2bf1ad0a880198a9dede37a6aa"]
            }
        ]
    """.trimIndent()

        val (command, _) = parse(json)
        command as REQ

        val subscriptionId: String = command.subscriptionId
        val filters: List<FiltersX> = command.filtersX

        assertEquals(subscriptionId, "8wHEWFsnIvKCWTb-4PMak")
        assertEquals(filters.size, 2)

        val tagsD: Set<String>? = filters[0].tags[TAG_D]
        assertEquals(
            tagsD,
            setOf(
                "3425e3a156471426798b80c1da1f148343c5c5b4d2ac452d3330a91b4619af65",
                "161498ed3277aa583c301288de5aafda4f317d2bf1ad0a880198a9dede37a6aa"
            )
        )

        /*
        val kinds = filters[0].kinds.map { it }
        println(kinds)
        println(kinds::class.java)
        println(listOf(1, 6, 16, 7, 9735, 2004, 30023))
        println(arrayListOf(1, 6, 16, 7, 9735, 2004, 30023)::class.java)

        assertEquals(
            filters[0].kinds.map { it },
            arrayListOf(1, 6, 16, 7, 9735, 2004, 30023)
        )
         */

        assertEquals(filters[0].limit, 50)

        assertEquals(
            filters[1].kinds, setOf(4L)
        )
        assertEquals(
            filters[1].tags[TAG_P], setOf("161498ed3277aa583c301288de5aafda4f317d2bf1ad0a880198a9dede37a6aa")
        )
    }


}
