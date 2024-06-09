package org.lnwza007.relay.modules

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Tags(
    @SerialName("#e")
    val e: Set<String> = emptySet(),
    @SerialName("#d")
    val d: Set<String> = emptySet(),
    @SerialName("#a")
    val a: Set<String> = emptySet(),
    @SerialName("#p")
    val p: Set<String> = emptySet(),
    @SerialName("#q")
    val q: Set<String> = emptySet(),
    @SerialName("#k")
    val k: Set<String> = emptySet(),
    @SerialName("#m")
    val m: Set<String> = emptySet(),
)

@Serializable
data class FiltersX(
    val ids: Set<String> = emptySet(),
    val authors: Set<String> = emptySet(),
    val kinds: Set<Long> = emptySet(),
    val tags: Tags = Tags(),
    val since: Long? = null,
    val until: Long? = null,
    val limit: Long? = null,
    val search: String? = null
)