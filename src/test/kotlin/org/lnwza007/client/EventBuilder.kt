package org.lnwza007.client

import com.fasterxml.jackson.databind.ObjectMapper
import fr.acinq.secp256k1.Secp256k1
import org.junit.jupiter.api.Test
import org.lnwza007.util.ShiftTo.randomBytes
import org.lnwza007.util.ShiftTo.toHex


class EventBuilder {


    lateinit var objectMapper: ObjectMapper

    @Test
    fun buildEvent() {

    }


}

fun main() {

    val privateKey = randomBytes(32)//.toHex()
    println(privateKey.toHex())

    var publicKey = Secp256k1.pubkeyCreate(privateKey)
    println(publicKey.toHex())
    publicKey =  Secp256k1.pubKeyCompress(publicKey).copyOfRange(1, 32)

    println(publicKey.toHex())

}