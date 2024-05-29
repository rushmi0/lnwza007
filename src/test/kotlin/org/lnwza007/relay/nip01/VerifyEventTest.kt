package org.lnwza007.relay.nip01

import org.lnwza007.relay.service.nip01.Transform.toEvent
import org.lnwza007.util.Schnorr
import org.lnwza007.util.ShiftTo.toJsonElementMap
import org.lnwza007.util.ShiftTo.toJsonString
import org.lnwza007.util.ShiftTo.toSha256

fun main() {

    val eventString = """
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

    val event = eventString.toJsonElementMap().toEvent()
    println(event)


    val drafField: String = arrayListOf(
        0,
        event?.pubkey,
        event?.createAt,
        event?.kind,
        event?.tags,
        event?.content
    ).toJsonString()

    println(drafField)

    val eventHash = drafField.toSha256()

    println(event?.id)

    val verify: Boolean = Schnorr.verify(eventHash, event?.pubkey!!, event?.signature!!)

    println(verify)

}