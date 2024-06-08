package org.lnwza007.relay.modules

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Tags(
    @SerialName("#e")
    val e: Set<String>? = null,
    @SerialName("#d")
    val d: Set<String>? = null,
    @SerialName("#a")
    val a: Set<String>? = null,
    @SerialName("#p")
    val p: Set<String>? = null
)

@Serializable
data class FiltersX(
    val ids: Set<String>? = null,
    val authors: Set<String>? = null,
    val kinds: Set<Long>? = null,
    val tags: Tags? = null,
    val since: Long? = null,
    val until: Long? = null,
    val limit: Long? = null,
    val search: String? = null
)
