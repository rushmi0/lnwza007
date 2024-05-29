package org.lnwza007.relay.modules

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
