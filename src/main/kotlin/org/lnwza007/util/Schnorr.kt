package org.lnwza007.util

import fr.acinq.secp256k1.Secp256k1
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.service.nip01.Transform.toEvent
import org.lnwza007.util.ShiftTo.fromHex
import org.lnwza007.util.ShiftTo.toHex
import org.lnwza007.util.ShiftTo.toJsonElementMap
import org.lnwza007.util.ShiftTo.toJsonString
import org.lnwza007.util.ShiftTo.toSha256

object Schnorr {

    /**
     * ฟังก์ชัน sign ใช้ในการสร้างลายเซ็น Schnorr สำหรับข้อมูลที่กำหนด
     * @param data ข้อมูลที่ต้องการลงลายเซ็น
     * @param privateKey คีย์ส่วนตัวที่ใช้ในการลงลายเซ็น
     * @param aux ข้อมูลเสริม (ถ้ามี) ที่ใช้ในการเพิ่มความปลอดภัย
     * @return ลายเซ็น Schnorr ที่ถูกสร้างขึ้นในรูปแบบ String (hex)
     */
    fun sign(
        data: String,
        privateKey: String,
        aux: ByteArray? = null
    ): String {
        return Secp256k1.signSchnorr(data.fromHex(), privateKey.fromHex(), aux).toHex()
    }

    /**
     * ฟังก์ชัน verify ใช้ในการตรวจสอบความถูกต้องของลายเซ็น Schnorr
     * @param data ข้อมูลที่ต้องการตรวจสอบ
     * @param publicKey คีย์สาธารณะที่ใช้ในการตรวจสอบ
     * @param signature ลายเซ็น Schnorr ที่ต้องการตรวจสอบ
     * @return ผลลัพธ์การตรวจสอบเป็น Boolean (true ถ้าลายเซ็นถูกต้อง, false ถ้าลายเซ็นไม่ถูกต้อง)
     */
    fun verify(
        data: String,
        publicKey: String,
        signature: String
    ): Boolean {
        return Secp256k1.verifySchnorr(signature.fromHex(), data.fromHex(), publicKey.fromHex())
    }


}


fun main() {
    val jsonString = """
        {
            "id": "0000005b0fc51e70b66db99ba1708b1a1b008c30db35d19d35146b3e09756029",
            "pubkey": "161498ed3277aa583c301288de5aafda4f317d2bf1ad0a880198a9dede37a6aa",
            "created_at": 1716617176,
            "kind": 1,
            "tags": [
              ["nonce","19735841","23"]
            ],
            "content": "My custom content",
            "sig": "954c662c9ee29ccad8a1f30d22b9a5cefcea774f72428ec7344b65e4f31fff24fc4dd0b7874a4d10a1a4c012de013df19a7c33018dda5f1207280f9a28193498"
          }
    """.trimIndent()

    val event = Json.decodeFromString<Event>(jsonString)
    println(event)

    val values = arrayListOf(
        0,
        event.pubkey,
        event.createAt,
        event.kind,
        event.tags,
        event.content
    )
    println(values)

    val valueString = values.toJsonString()
    println(valueString)

    val sha256 = valueString.toSha256()
    val newData = jsonString.toJsonElementMap().toEvent()
    println(newData)

    val id = newData?.id
    val publicKey = newData?.pubkey
    val signature = newData?.signature
    val tag = newData?.tags

    println("$id\n$publicKey\n$signature")
    println("tag : $tag")
    println(tag?.map { it[1] })

    val isValid = Schnorr.verify(sha256, publicKey!!, signature!!)
    println("Is Valid: $isValid\n")

}
