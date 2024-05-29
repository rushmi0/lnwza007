package org.lnwza007.relay.modules


interface NostrFieldTypeProvider {
    val fieldType: Class<*>
}


interface NostrField : NostrFieldTypeProvider {
    val fieldName: String
    val fieldCollectionType: Class<*>? get() = null
    val nestedFieldType: Class<*>? get() = null
}


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


enum class EventValidateField(
    override val fieldName: String,
    override val fieldType: Class<*>,
    override val fieldCollectionType: Class<*>? = null,
    override val nestedFieldType: Class<*>? = null
) : NostrField {
    ID("id", String::class.java),
    PUBKEY("pubkey", String::class.java),
    CREATE_AT("created_at", Long::class.java),
    CONTENT("content", String::class.java),
    KIND("kind", Int::class.java),
    TAGS("tags", List::class.java, List::class.java, String::class.java),
    SIGNATURE("sig", String::class.java)
}
