package org.lnwza007.relay.nip01

import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.FiltersX
import org.lnwza007.relay.modules.FiltersXValidateField
import org.lnwza007.relay.service.nip01.Transform.toEvent
import org.lnwza007.relay.service.nip01.Transform.toFiltersX
import org.lnwza007.relay.service.nip01.VerificationFactory
import org.lnwza007.util.ShiftTo.toJsonElementMap

internal class VerificationFactoryTest {

    private val validateField = VerificationFactory()

    @Test
    fun `test mapToObject with valid data`() {
        val jsonString = """
            {"ids": ["e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3"], "kinds": [4]}
        """.trimIndent()
        val map = jsonString.toJsonElementMap()

        val result = validateField.mapToObject(map, FiltersXValidateField.entries.toTypedArray()) { map ->
            val ids = map["ids"]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet()
            val kinds = map["kinds"]?.jsonArray?.map { it.jsonPrimitive.int }?.toSet()
            FiltersX(ids, null, kinds, null, null, null, null)
        }

        val expected = FiltersX(
            setOf("e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3"),
            null,
            setOf(4),
            null,
            null,
            null,
            null
        )

        assertEquals(expected, result)
    }

    @Test
    fun `test mapToObject with invalid data`() {
        val jsonString = """
            {"search": "purple", "kinds": [1], "status": true, "since": 1715181359}
        """.trimIndent()
        val map = jsonString.toJsonElementMap()

        val result = validateField.mapToObject(map, FiltersXValidateField.entries.toTypedArray()) { map ->
            val ids = map["ids"]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet()
            val kinds = map["kinds"]?.jsonArray?.map { it.jsonPrimitive.int }?.toSet()
            FiltersX(ids, null, kinds, null, null, null, null)
        }

        assertNull(result)
    }

    @Test
    fun `test mapToFiltersX with valid data`() {
        val jsonString = """
            {"ids": ["e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3"], "kinds": [4]}
        """.trimIndent()
        val map = jsonString.toJsonElementMap()

        val result = map.toFiltersX()

        val objFiltersX = FiltersX(
            setOf("e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3"),
            null,
            setOf(4),
            null,
            null,
            null,
            null
        )

        val expected = Pair(null, objFiltersX)

        assertEquals(expected, result)
    }

    @Test
    fun `test mapToFiltersX with invalid data`() {
        val jsonString = """
            {"search": "purple", "kinds": [1], "status": true, "since": 1715181359}
        """.trimIndent()
        val map = jsonString.toJsonElementMap()

        val result = map.toFiltersX()

        assertNull(result)
    }

    @Test
    fun `test mapToEvent with valid data`() {
        val jsonString = """
           {
              "id":"0000005b0fc51e70b66db99ba1708b1a1b008c30db35d19d35146b3e09756029",
              "pubkey":"161498ed3277aa583c301288de5aafda4f317d2bf1ad0a880198a9dede37a6aa",
              "created_at":1716617176,
              "kind":1,
              "tags":[
                ["nonce","19735841","23"]
               ],
              "content":"My custom content",
              "sig":"954c662c9ee29ccad8a1f30d22b9a5cefcea774f72428ec7344b65e4f31fff24fc4dd0b7874a4d10a1a4c012de013df19a7c33018dda5f1207280f9a28193498"
           }
        """.trimIndent()

        val result = jsonString.toJsonElementMap().toEvent()

        val expected = Event(
            id = "0000005b0fc51e70b66db99ba1708b1a1b008c30db35d19d35146b3e09756029",
            pubkey = "161498ed3277aa583c301288de5aafda4f317d2bf1ad0a880198a9dede37a6aa",
            createAt = 1716617176,
            content = "My custom content",
            kind = 1,
            tags = listOf(
                listOf("nonce", "19735841", "23")
            ),
            signature = "954c662c9ee29ccad8a1f30d22b9a5cefcea774f72428ec7344b65e4f31fff24fc4dd0b7874a4d10a1a4c012de013df19a7c33018dda5f1207280f9a28193498"
        )

        assertNull(result[0])
        assertEquals(expected, result[1])
    }
}