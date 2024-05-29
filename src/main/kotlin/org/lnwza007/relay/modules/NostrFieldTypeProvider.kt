package org.lnwza007.relay.modules

interface NostrFieldTypeProvider {
    val fieldType: Class<*>
}

interface NostrField : NostrFieldTypeProvider {
    val fieldName: String
    val fieldCollectionType: Class<*>? get() = null
    val nestedFieldType: Class<*>? get() = null
}
