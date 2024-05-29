//package org.lnwza007.relay.service.nip01
//
//import org.lnwza007.relay.modules.Event
//import org.lnwza007.relay.service.nip01.Transform.toEvent
//import org.lnwza007.relay.service.nip13.ProofOfWork
//import org.lnwza007.util.Schnorr
//import org.lnwza007.util.ShiftTo.fromHex
//import org.lnwza007.util.ShiftTo.toJsonElementMap
//import org.lnwza007.util.ShiftTo.toJsonString
//import org.lnwza007.util.ShiftTo.toSha256
//
//object EventValidator {
//
//    /**
//     * ตรวจสอบว่าขนาดของ ID, Public Key และ Signature ถูกต้องหรือไม่
//     */
//    private fun isSizeValid(event: Event): Boolean {
//        return event.id?.fromHex()?.size == 32 && event.pubkey?.fromHex()?.size == 32&& event.signature?.fromHex()?.size == 64
//    }
//
//    /**
//     * ตรวจสอบ Proof of Work ของ event ว่าถูกต้องตามที่กำหนดหรือไม่
//     */
//    private fun isProofOfWorkValid(event: Event): Boolean {
//        val nonceTag = event.tags?.find { it[0] == "nonce" } ?: return true
//        val difficulty = nonceTag[2].toIntOrNull() ?: return false
//        return ProofOfWork.checkProofOfWork(event.id!!, difficulty.toLong())
//    }
//
//    /**
//     * ตรวจสอบว่าลายเซ็นของ event ถูกต้องหรือไม่
//     */
//    private fun isSignatureValid(event: Event): Boolean {
//        val draftField: String = arrayListOf(
//            0,
//            event.pubkey,
//            event.createAt,
//            event.kind,
//            event.tags,
//            event.content
//        ).toJsonString()
//
//        val eventHash = draftField.toSha256()
//        return Schnorr.verify(eventHash, event.pubkey!!, event.signature!!)
//    }
//
//    /**
//     * ฟังก์ชันตรวจสอบ event ทั้งหมด
//     */
//    fun validateEvent(event: Event): Boolean {
//        if (!isSizeValid(event)) return false
//        if (!isProofOfWorkValid(event)) return false
//        if (!isSignatureValid(event)) return false
//        return true
//    }
//}
//
//fun main() {
//    val eventString = """
//       {
//          "id":"0000005b0fc51e70b66db99ba1708b1a1b008c30db35d19d35146b3e09756029",
//          "pubkey":"161498ed3277aa583c301288de5aafda4f317d2bf1ad0a880198a9dede37a6aa",
//          "created_at":1716617176,
//          "kind":1,
//          "tags":[
//            ["nonce","19735841","23"]
//           ],
//          "content":"My custom content",
//          "sig":"954c662c9ee29ccad8a1f30d22b9a5cefcea774f72428ec7344b65e4f31fff24fc4dd0b7874a4d10a1a4c012de013df19a7c33018dda5f1207280f9a28193498"
//       }
//    """.trimIndent()
//
//    val d = """
//       {
//        "created_at": 1716448321,
//        "kind": 0,
//        "tags": [["alt", "User profile for lnwza007"]],
//        "content": "{\"name\":\"lnwza007\",\"gender\":\"\",\"area\":\"\",\"picture\":\"https://image.nostr.build/552b5424ebd3c66be6f588e08c2f427e04423f11e80514414215b5ae00877b28.gif\",\"lud16\":\"rushmi0@getalby.com\",\"website\":\"https://github.com/rushmi0\",\"display_name\":\"lnwza007\",\"banner\":\"\",\"about\":\"แดดกรุงเทพที่ร้อนจ้า ยังแพ้ตัวข้าที่ร้อน sat\"}",
//        "pubkey": "e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3",
//        "id": "ecfdf5d329ae69bdca40f04a33a8f8447b83824f958a8db926430cd8b2aeb350",
//        "sig": "6a7898997ceb936fb6f660848baf8185f84ab22ff45aa3fc36eabad577bb4fae739bfdcd3d428d52146c6feaf9264bbc8f82121ddb8eeb85ce242ff79a1b0948"
//       }
//    """.trimIndent()
//
//    val event = d.toJsonElementMap().toEvent()
//    if (event != null) {
//        val isValid = EventValidator.validateEvent(event)
//        println("Event is valid: $isValid")
//    } else {
//        println("Invalid event data")
//    }
//}