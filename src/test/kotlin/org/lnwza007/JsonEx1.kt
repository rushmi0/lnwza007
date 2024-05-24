package org.lnwza007

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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