package org.lnwza007.relay.modules

enum class MessageType(
    val fieldName: String
) {
    EVENT("EVENT"),
    OK("OK"),
    EOSE("EOSE"),
    CLOSED("CLOSED"),
    NOTICE("NOTICE")
}