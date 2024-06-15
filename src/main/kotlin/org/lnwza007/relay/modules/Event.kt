package org.lnwza007.relay.modules

import io.micronaut.serde.annotation.Serdeable.Deserializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.lnwza007.relay.service.nip01.Transform.toJsonString
import org.lnwza007.util.Schnorr
import org.lnwza007.util.ShiftTo.generateId

@Deserializable
@Serializable
data class Event(
    val id: String? = null,
    @SerialName("pubkey")
    val pubKey: String? = null,
    @SerialName("created_at")
    val createAt: Long? = null,
    val kind: Long? = null,
    val tags: List<List<String>>? = null,
    val content: String? = null,
    @SerialName("sig")
    val signature: String? = null
) {


    fun isValidEventId(): Pair<Boolean, String> {
        val actualId = generateId(this)
        return if (this.id != actualId) {
            Pair(false, "Invalid: actual event id $actualId")
        } else {
            Pair(true, "")
        }
    }

    fun isValidSignature(): Pair<Boolean, String> {
        val eventId = if (isValidEventId().first) id!! else generateId(this)
        if (!Schnorr.verify(eventId, pubKey!!, signature!!)) {
            val warning = """
                |Invalid: bad signature
                |  Event: ${this.toJsonString()}
                |  Actual: ${generateId(this)}
            """.trimIndent()
            return Pair(false, warning)
        }
        return Pair(true, "")
    }

}
