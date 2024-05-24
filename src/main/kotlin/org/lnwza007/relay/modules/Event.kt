package org.lnwza007.relay.modules

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val id: String? = null,
    val pubkey: String? = null,
    @SerialName("created_at")
    val createAt: Long? = null,
    val content: String? = null,
    val kind: Int? = null,
    val tags: List<List<String>>? = null,
    @SerialName("sig")
    val signature: String? = null
)
