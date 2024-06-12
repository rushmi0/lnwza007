package org.lnwza007.relay.nip01


import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonElement
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.lnwza007.relay.policy.EventValidateField
import org.lnwza007.relay.policy.FiltersXValidateField
import org.lnwza007.relay.service.nip01.VerificationFactory
import org.lnwza007.util.ShiftTo.toJsonElementMap

class VerificationFactoryTest {

    private val verifyData = VerificationFactory()

    @Test
    fun `test invalid event data`()  {
        val invalidData = """
            {
                "kind":0,
                "created_at":"1716448321",
                "tags":[["alt","User profile for lnwza007"]],
                "content":"{\"name\":\"lnwza007\",\"gender\":\"\",\"area\":\"\",\"picture\":\"https://image.nostr.build/552b5424ebd3c66be6f588e08c2f427e04423f11e80514414215b5ae00877b28.gif\",\"lud16\":\"rushmi0@getalby.com\",\"website\":\"https://github.com/rushmi0\",\"display_name\":\"lnwza007\",\"banner\":\"\",\"about\":\"แดดกรุงเทพที่ร้อนจ้า ยังแพ้ตัวข้าที่ร้อน sat\"}",
                "pubkey":"e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3",
                "id":"ecfdf5d329ae69bdca40f04a33a8f8447b83824f958a8db926430cd8b2aeb350",
                "sig":"6a7898997ceb936fb6f660848baf8185f84ab22ff45aa3fc36eabad577bb4fae739bfdcd3d428d52146c6feaf9264bbc8f82121ddb8eeb85ce242ff79a1b0948"
            }
        """.trimIndent()

        val jsonEvent: Map<String, JsonElement> = invalidData.toJsonElementMap()
        val commandEvent: Pair<Boolean, String?> = verifyData.validateDataType(jsonEvent, EventValidateField.entries.toTypedArray())
        assertEquals(
            Pair(false, "Invalid: data type at [created_at] field"),
            commandEvent
        )
    }

    @Test
    fun `test valid event data`() = runBlocking {
        val validData = """
            {
                "id": "0000005b0fc51e70b66db99ba1708b1a1b008c30db35d19d35146b3e09756029",
                "pubkey": "161498ed3277aa583c301288de5aafda4f317d2bf1ad0a880198a9dede37a6aa",
                "created_at": 1716617176,
                "kind": 1,
                "tags": [
                  ["nonce","19735841","23"]
                ],
                "content": "My custom content ",
                "sig": "954c662c9ee29ccad8a1f30d22b9a5cefcea774f72428ec7344b65e4f31fff24fc4dd0b7874a4d10a1a4c012de013df19a7c33018dda5f1207280f9a28193498"
            }
        """.trimIndent()

        val jsonEvent: Map<String, JsonElement> = validData.toJsonElementMap()
        val commandEvent = verifyData.validateDataType(jsonEvent, EventValidateField.entries.toTypedArray())
        assertEquals(
            Pair(false, "Invalid: signature"),
            commandEvent
        )
    }

    @Test
    fun `test valid filtersX data`() = runBlocking {
        val jsonREQ = """
            {
                "authors": [
                  "8c3a51f90fe05d694a1efe95e0d31b1b00f3314029d708b120e7f8d4a983a89c",
                  "e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3"
                ],
                "kinds": [1]
            }
        """.trimIndent()

        val jsonElementREQ: Map<String, JsonElement> = jsonREQ.toJsonElementMap()
        val commandREQ: Pair<Boolean, String?> = verifyData.validateDataType(
            jsonElementREQ,
            FiltersXValidateField.entries.toTypedArray()
        )
        assertEquals(
            Pair(true, "Not yet implemented"),
            commandREQ
        )
    }

    @Test
    fun `test valid filtersX data with since`() = runBlocking {
        val jsonREQ = """
            {
                "authors": [
                  "e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3"
                ],
                "kinds": [4],
                "since": 1715181359
            }
        """.trimIndent()

        val jsonElementREQ: Map<String, JsonElement> = jsonREQ.toJsonElementMap()
        val commandREQ: Pair<Boolean, String?> = verifyData.validateDataType(
            jsonElementREQ,
            FiltersXValidateField.entries.toTypedArray()
        )
        assertEquals(
            Pair(true, "Not yet implemented"),
            commandREQ
        )
    }
}
