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
class KeyGenerationController {

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

        // สร้าง coroutineScope เพื่อรวม coroutine ทั้งหมด
        return coroutineScope {
            val jobList = mutableListOf<Deferred<GenerateKey>>()

            // สร้างและเริ่ม coroutine หลายๆ ตัว
            repeat(10_000) {
                val job = async { generateNpubKeyCoroutine(require) }
                jobList.add(job)
            }

            // รอผลลัพธ์จากทุก coroutine และเก็บผลลัพธ์ลงใน List
            val resultList = jobList.awaitAll()

            // ค้นหาผลลัพธ์ที่ตรงกับเงื่อนไข
            resultList.first { it.npub.startsWith(require) }
        }
    }

    private fun generateNpubKeyCoroutine(require: String): GenerateKey {
        var attempts = 0
        while (true) {
            val privkey: ByteArray = randomBytes(32)
            val pubkeys = Secp256k1.pubkeyCreate(privkey)
            val compressed = Secp256k1.pubKeyCompress(pubkeys)
            val xOnly = compressed.copyOfRange(1, 33)
            val npub = Bech32.encode("npub", xOnly.toHex())
            attempts++
            LOG.info("npub: $npub")

            if (npub.startsWith(require)) {
                return GenerateKey(
                    privkey = privkey.toHex(),
                    xOnly = xOnly.toHex(),
                    npub = npub,
                    attempts = attempts.toLong()
                )
            }
        }
    }

    companion object {
        private val LOG: Logger = LoggerFactory.getLogger(KeyGenerationController::class.java)
    }
}
