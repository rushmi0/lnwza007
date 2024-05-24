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
import org.lnwza007.relay.service.nip01.ValidateField
import org.lnwza007.util.ShiftTo.toJsonElementMap

internal class ValidateFieldTest {

    private val validateField = ValidateField()

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
              "id": "000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358",
              "pubkey": "a48380f4cfcc1ad5378294fcac36439770f9c878dd880ffa94bb74ea54a6f243",
              "created_at": 1651794653,
              "kind": 1,
              "tags": [
                ["nonce", "776797", "21"]
              ],
              "content": "It's just me mining my own business",
              "sig": "284622fc0a3f4f1303455d5175f7ba962a3300d136085b9566801bc2e0699de0c7e31e44c81fb40ad9049173742e904713c3594a1da0fc5d2382a25c11aba977"
            }
        """.trimIndent()
        val map = jsonString.toJsonElementMap()

        val result = map.toEvent()

        val expected = Event(
            id = "000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358",
            pubkey = "a48380f4cfcc1ad5378294fcac36439770f9c878dd880ffa94bb74ea54a6f243",
            createAt = 1651794653,
            content = "It's just me mining my own business",
            kind = 1,
            tags = listOf(listOf("nonce", "776797", "21")),
            signature = "284622fc0a3f4f1303455d5175f7ba962a3300d136085b9566801bc2e0699de0c7e31e44c81fb40ad9049173742e904713c3594a1da0fc5d2382a25c11aba977"
        )

        assertEquals(expected, result)
    }
}