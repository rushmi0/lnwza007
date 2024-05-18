package org.lnwza007.relay.modules

import kotlinx.serialization.Serializable


@Serializable
data class Event(
    val id: String,
    val pubkey: String,
    val createdAt: Long,
    val kind: Int,
    val tags: List<List<String>>,//Array<Array<String>>
    val content: String,
    val sig: String
)
