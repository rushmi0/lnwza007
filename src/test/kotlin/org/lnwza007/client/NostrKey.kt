//package org.lnwza007.client
//
//import fr.acinq.secp256k1.Secp256k1
//import io.micronaut.serde.annotation.Serdeable.Deserializable
//import kotlinx.serialization.Serializable
//import org.junit.jupiter.api.Test
//import org.lnwza007.util.Bech32
//import org.lnwza007.util.ShiftTo.randomBytes
//import org.lnwza007.util.ShiftTo.toHex
//
//class NostrKey {
//
//    @Serializable
//    @Deserializable
//    data class GenerateKey(
//        val privkey: String,
//        val xOnly: String,
//        val npub: String,
//        val attempts: Long
//    )
//
//
//    @Test
//    fun `test nostr key (npub)`() {
//        val require = "npub1lnwza007"
//        var foundNpub = false
//        var attempts = 0
//
//        println("Starting....")
//        while (!foundNpub) {
//            val privkey: ByteArray = randomBytes(32)
//            val pubkeys = Secp256k1.pubkeyCreate(privkey)
//            val compressed = Secp256k1.pubKeyCompress(pubkeys)
//            val xOnly = compressed.copyOfRange(1, 33)
//            val npub = Bech32.encode(xOnly.toHex())
//            attempts++
//
//            if (npub.startsWith(require)) {
//                println("Found matching npub1lnwza007:")
//                println("Private key: ${privkey.toHex()}")
//                println("Public key X only: ${xOnly.toHex()}")
//                println("npub: $npub")
//                foundNpub = true
//            }
//        }
//        println("Total attempts: $attempts")
//    }
//}
//// localhost:6624/genkey/npub/npub1lnwza007