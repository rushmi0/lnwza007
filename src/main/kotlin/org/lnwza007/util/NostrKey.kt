package org.lnwza007.util

import fr.acinq.secp256k1.Secp256k1
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.serde.annotation.Serdeable.Deserializable
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import org.lnwza007.util.ShiftTo.randomBytes
import org.lnwza007.util.ShiftTo.toHex
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Controller("/genkey")
class NostrKey {

    @Serializable
    @Deserializable
    data class GenerateKey(
        val privkey: String,
        val xOnly: String,
        val npub: String,
        val attempts: Long
    )


    @Get("/npub/{require}")
    suspend fun generateNpubKey(@PathVariable require: String): GenerateKey {
        LOG.info("Generating npub key")
        val worker = 1_000_000_000

        return CoroutineManager.parallelIO(worker) {
            val jobList = mutableListOf<Deferred<GenerateKey>>()

            repeat(worker) {
                val job = async { generateNpubKeyCoroutine(require) }
                jobList.add(job)
            }

            val resultList = jobList.awaitAll()

            resultList.first { it.npub.startsWith(require) }
        }
    }


    private fun generateNpubKeyCoroutine(require: String): GenerateKey {
        var attempts = 0
        LOG.info("Starting generation coroutine")

        while (true) {
            val privkey = randomBytes(32)
            val pubkeys = Secp256k1.pubkeyCreate(privkey)
            val compressed = Secp256k1.pubKeyCompress(pubkeys)
            val xOnly = compressed.copyOfRange(1, 33)
            val npub = Bech32.encode("npub", xOnly.toHex())
            attempts++
            LOG.info("[$attempts] npub: $npub")

            if (npub.startsWith(require)) {
                val result =  GenerateKey(
                    privkey = privkey.toHex(),
                    xOnly = xOnly.toHex(),
                    npub = npub,
                    attempts = attempts.toLong()
                )

                LOG.info("Coroutine finished successfully\n$result")
                return result
            }
        }
    }


    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(NostrKey::class.java)
    }
}
