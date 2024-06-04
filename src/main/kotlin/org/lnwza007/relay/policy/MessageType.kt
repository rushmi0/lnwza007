package org.lnwza007.relay.policy

enum class MessageType(
    val fieldName: String
) {
    EVENT("EVENT"),
    OK("OK"),
    EOSE("EOSE"),
    CLOSED("CLOSED"),
    NOTICE("NOTICE")
}