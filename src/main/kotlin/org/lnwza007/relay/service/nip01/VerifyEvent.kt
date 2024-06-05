package org.lnwza007.relay.service.nip01

import kotlinx.serialization.json.JsonElement
import org.lnwza007.relay.service.nip01.Transform.convertToEventObject
import org.lnwza007.util.Schnorr
import org.lnwza007.util.ShiftTo.toJsonString
import org.lnwza007.util.ShiftTo.toSha256

data class VerifyEvent(val receive: Map<String, JsonElement>) {

    private val obj = convertToEventObject(receive)

    private val id: String by lazy {
        arrayListOf(
            0,
            obj.pubkey,
            obj.createAt,
            obj.kind,
            obj.tags,
            obj.content
        ).toJsonString().toSha256()
    }

    fun checkId(): Pair<Boolean, String?> = if (obj.id != id) Pair(false, "failed") else Pair(true, null)

    fun checkSignature(): Pair<Boolean, String?> {
        val isValid = Schnorr.verify(obj.id!!, obj.pubkey!!, obj.signature!!)
        return if (isValid) {
            Pair(true, null)
        } else {
            Pair(false, "invalid: bad signature")
        }
    }




}
