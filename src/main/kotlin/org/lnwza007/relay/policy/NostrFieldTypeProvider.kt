package org.lnwza007.relay.policy

interface NostrFieldTypeProvider {
    val fieldType: Class<*>
}

interface NostrField : NostrFieldTypeProvider {
    val fieldName: String
    val fieldCollectionType: Class<*>? get() = null
}
