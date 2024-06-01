package org.lnwza007.relay.modules

import kotlinx.serialization.Serializable

@Serializable
data class FiltersX(
    val ids: Set<String>? = null,
    val authors: Set<String>? = null,
    val kinds: Set<Long>? = null,
    val since: Long? = null,
    val until: Long? = null,
    val limit: Long? = null,
    val search: String? = null
)
