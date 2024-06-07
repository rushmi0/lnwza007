package org.lnwza007

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import org.lnwza007.util.Schnorr
import org.lnwza007.util.ShiftTo.toSha256
import java.util.*


fun main() {
    val pubkey = "a48380f4cfcc1ad5378294fcac36439770f9c878dd880ffa94bb74ea54a6f243"
    val createdAt: Long = 1651794653
    val kind = 1
    val tags = listOf(
        listOf("nonce", "776797", "21")
    )
    val content = "It's just me mining my own business"

    val fields = arrayListOf(
        0,
        pubkey,
        createdAt,
        kind,
        tags,
        content
    )

    println(fields)

    val subscriptionId: String = UUID.randomUUID().toString().substring(0..10)
    println(subscriptionId)

    val objectMapper = jacksonObjectMapper()
    val stringJSON = objectMapper.writeValueAsString(fields)
    println(stringJSON)

    val hash = stringJSON.toSha256()
    println(hash)

    val jsonString = """
        {
            "created_at": "1716448321",
            "kind": 0,
            "tags": [["alt", "User profile for lnwza007"]],
            "content": "{\"name\":\"lnwza007\",\"gender\":\"\",\"area\":\"\",\"picture\":\"https://image.nostr.build/552b5424ebd3c66be6f588e08c2f427e04423f11e80514414215b5ae00877b28.gif\",\"lud16\":\"rushmi0@getalby.com\",\"website\":\"https://github.com/rushmi0\",\"display_name\":\"lnwza007\",\"banner\":\"\",\"about\":\"แดดกรุงเทพที่ร้อนจ้า ยังแพ้ตัวข้าที่ร้อน sat\"}",
            "pubkey": "e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3",
            "id": "ecfdf5d329ae69bdca40f04a33a8f8447b83824f958a8db926430cd8b2aeb350",
            "sig": "6a7898997ceb936fb6f660848baf8185f84ab22ff45aa3fc36eabad577bb4fae739bfdcd3d428d52146c6feaf9264bbc8f82121ddb8eeb85ce242ff79a1b0948"
        }
    """
    val jsonObject = Json.parseToJsonElement(jsonString).jsonObject

    // Convert JsonObject to Map<String, JsonElement>
    val jsonObjectMap: Map<String, JsonElement> = jsonObject.toMap()

    // Print the map to verify
    jsonObjectMap.forEach { (key, value) ->
        println("$key : $value")
    }

//    val json = Json.encodeToString(fields)
//    val hashedData = json.toSha256()
//
//    println("JSON String: $json")
//    println("Hashed Data: $hashedData")
//
//    val sig = "284622fc0a3f4f1303455d5175f7ba962a3300d136085b9566801bc2e0699de0c7e31e44c81fb40ad9049173742e904713c3594a1da0fc5d2382a25c11aba977"
//
//    val isValidSignature = Schnorr.verify(
//        data = hashedData,
//        publicKey = pubkey,
//        signature = sig
//    )
//
//    println("Signature is valid: $isValidSignature")
}