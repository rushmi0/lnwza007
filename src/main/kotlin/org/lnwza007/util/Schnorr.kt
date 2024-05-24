package org.lnwza007.util

import fr.acinq.secp256k1.Secp256k1
import kotlinx.serialization.json.Json
import org.lnwza007.relay.modules.Event
import org.lnwza007.util.ShiftTo.toHex

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
        return Secp256k1.signSchnorr(data.toByteArray(), privateKey.toByteArray(), aux).toHex()
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
        return Secp256k1.verifySchnorr(signature.toByteArray(), data.toByteArray(), publicKey.toByteArray())
    }


}


/*
fun main() {
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

    val event = Json.decodeFromString<Event>(jsonString)

// ตรวจสอบว่าทำงานถูกต้องโดยการพิมพ์ข้อมูลทั้งหมด
    println(event)

// แปลงค่าต่างๆมาเรียงใน list
    val values = arrayListOf(
        0,
        event.pubKey,
        event.createdAt,
        event.kind,
        event.tags,
        event.content
    )
    println(values)

    // แปลงเป็น JSON string
//    val jsonValues = Event.toJson(values)
//
//    // ทำการ hash ด้วย SHA-256
//    val hash = jsonValues.toSha256()
//
//    // ทำการ verify ลายเซ็น
//    val isSignatureValid = Schnorr.verify(hash, event.pubkey, event.sig)
//    println("Is Signature Valid: $isSignatureValid")
}

 */
