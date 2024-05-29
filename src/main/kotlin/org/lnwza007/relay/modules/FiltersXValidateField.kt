package org.lnwza007.relay.modules

enum class FiltersXValidateField(
    override val fieldName: String,
    override val fieldType: Class<*>,
    override val fieldCollectionType: Class<*>? = null
) : NostrField {
    IDS("ids", String::class.java, Set::class.java),
    AUTHORS("authors", String::class.java, Set::class.java),
    KINDS("kinds", Int::class.java, Set::class.java),
    SINCE("since", Long::class.java),
    UNTIL("until", Long::class.java),
    LIMIT("limit", Long::class.java),
    SEARCH("search", String::class.java)
}
