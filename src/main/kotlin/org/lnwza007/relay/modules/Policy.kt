package org.lnwza007.relay.modules

interface EnumField {
    val fieldName: String
}

enum class FiltersXValidateField(override val fieldName: String) : EnumField {
    IDS("ids"),              // String
    AUTHORS("authors"),      // String
    KINDS("kinds"),          // Int
    SINCE("since"),          // Long
    UNTIL("until"),          // Long
    LIMIT("limit"),          // Long
    SEARCH("search")         // String
}

enum class EventValidateField(override val fieldName: String) : EnumField {
    ID("id"),                // String
    PUBKEY("pubkey"),        // String
    CREATE_AT("created_at"), // Long
    CONTENT("content"),      // String
    KIND("kind"),            // Int
    TAGS("tags"),            // List<List<String>>
    SIGNATURE("sig"),        // String
}
