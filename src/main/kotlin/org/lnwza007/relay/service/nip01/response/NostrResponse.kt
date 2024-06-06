package org.lnwza007.relay.service.nip01.response


import org.lnwza007.relay.modules.Event
import org.lnwza007.util.ShiftTo.toJsonString

sealed class NostrResponse<out T> {

    data class EVENT(val subscriptionId: String, val event: Event) : NostrResponse<Unit>()

    data class OK(val eventId: String, val isSuccess: Boolean, val message: String) : NostrResponse<Unit>()

    data class EOSE(val subscriptionId: String) : NostrResponse<Unit>()

    data class CLOSED(val subscriptionId: String, val message: String) : NostrResponse<Unit>()

    data class NOTICE(val message: String) : NostrResponse<Unit>()

    fun toJson(): String {
        return when (this) {
            is EVENT -> listOf("EVENT", subscriptionId, event).toJsonString()
            is OK -> listOf("OK", eventId, isSuccess, message).toJsonString()
            is EOSE -> listOf("EOSE", subscriptionId).toJsonString()
            is CLOSED -> listOf("CLOSED", subscriptionId, message).toJsonString()
            is NOTICE -> listOf("NOTICE", message).toJsonString()
        }
    }
    
}

fun main() {

    val event = Event(
        id = "000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358",
        pubkey = "a48380f4cfcc1ad5378294fcac36439770f9c878dd880ffa94bb74ea54a6f243",
        createAt = 1651794653,
        kind = 1,
        tags = listOf(listOf("nonce", "776797", "20")),
        content = "It's just me mining my own business",
        signature = "284622fc0a3f4f1303455d5175f7ba962a3300d136085b9566801bc2e0699de0c7e31e44c81fb40ad9049173742e904713c3594a1da0fc5d2382a25c11aba977"
    )

    val res1 = NostrResponse.EVENT("b1a649ebe8", event).toJson()
    val res2 = NostrResponse.OK("000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358", true, "").toJson()
    val res3 = NostrResponse.OK("000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358", false, "duplicate: already have this event").toJson()
    val res4 = NostrResponse.OK("000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358", false, "invalid: missing fields").toJson()
    val res5 = NostrResponse.EOSE("b1a649ebe8").toJson()
    val res6 = NostrResponse.NOTICE("unrecognised filter item").toJson()
    val res7 = NostrResponse.CLOSED("b1a649ebe8", "unsupported: filter contains unknown fields").toJson()

    println(res1)
    println(res2)
    println(res3)
    println(res4)
    println(res5)
    println(res6)
    println(res7)

}
