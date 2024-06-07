package org.lnwza007.relay.service.nip01

import jakarta.inject.Inject
import org.slf4j.LoggerFactory

class BasicProtocolFlow @Inject constructor(
//    private val nip09: EventDeletion,
//    private val nip13: ProofOfWork
) {

    suspend fun onEvent() {

    }

    suspend fun onRequest() {

    }

    fun onClose() {

    }

    fun onUnknown() {

    }

    private val LOG = LoggerFactory.getLogger(BasicProtocolFlow::class.java)

}
