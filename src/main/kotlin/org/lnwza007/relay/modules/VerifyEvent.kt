package org.lnwza007.relay.modules


import fr.acinq.secp256k1.Secp256k1
import java.math.BigInteger
import java.security.MessageDigest

class VerifyEvent {







}

fun signSchnorr(
    data: ByteArray,
    privKey: ByteArray
): ByteArray {
    return Secp256k1.signSchnorr(data, privKey, null)
}
/*
* public abstract fun verifySchnorr(
    signature: ByteArray,
    data: ByteArray,
    pub: ByteArray
):
* */
fun verifySchnorr(
    data: ByteArray,
    pubKey: ByteArray,
    sig: ByteArray
): Boolean {
    return Secp256k1.verifySchnorr(sig, data, pubKey)
}

fun sha256(data: ByteArray): ByteArray {
    return MessageDigest.getInstance("SHA-256").digest(data)
}

fun main() {

    val privateKey = BigInteger("51910214170999450186530030419309914520002218989473598981700580551132384417562")
    println("Private Key: $privateKey")

    val pubKeyX = BigInteger("54937464590658530654488624268151724241105264383655924818230768164485909069475")
    println("Public Key X: $pubKeyX")

    val message = sha256("I am a fish".toByteArray())

    val signature: ByteArray = signSchnorr(message, privateKey.toByteArray())
    println("Signature size ${signature.size}")

    val verify = verifySchnorr(message, pubKeyX.toByteArray(), signature)
    println("Verify: $verify")
}
