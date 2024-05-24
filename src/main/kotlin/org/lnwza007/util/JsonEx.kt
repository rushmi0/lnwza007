import kotlinx.serialization.json.JsonElement

//package org.lnwza007.util
//
//import kotlinx.serialization.Serializable
//import kotlinx.serialization.SerialName
//import kotlinx.serialization.json.*
//import java.util.ArrayDeque
//import java.util.Queue
//
//interface EnumField {
//    val fieldName: String
//}
//
//enum class FiltersXValidateField(override val fieldName: String) : EnumField {
//    IDS("ids"),         // String
//    AUTHORS("authors"), // String
//    KINDS("kinds"),     // Int
//    SINCE("since"),     // Long
//    UNTIL("until"),     // Long
//    LIMIT("limit"),     // Long
//    SEARCH("search")    // String
//}
//
//enum class EventValidateField(override val fieldName: String) : EnumField {
//    ID("id"),                // String
//    PUBKEY("pubkey"),        // String
//    CREATE_AT("created_at"), // Long
//    CONTENT("content"),      // String
//    KIND("kind"),            // Int
//    TAGS("tags"),            // List<List<String>>
//    SIGNATURE("sig"),        // String
//}
//
//@Serializable
//data class FiltersX(
//    val ids: Set<String>? = null,
//    val authors: Set<String>? = null,
//    val kinds: Set<Int>? = null,
//    val since: Long? = null,
//    val until: Long? = null,
//    val limit: Long? = null,
//    val search: String? = null
//)
//
//@Serializable
//data class Event(
//    val id: String? = null,
//    val pubkey: String? = null,
//    @SerialName("created_at")
//    val createAt: Long? = null,
//    val content: String? = null,
//    val kind: Int? = null,
//    val tags: List<List<String>>? = null,
//    val signature: String? = null
//)
//
//fun buildUpdateQueue(data: Map<String, JsonElement>, validateFields: Array<out EnumField>): Pair<Queue<String>, Boolean> {
//    val updateQueue = ArrayDeque<String>()
//    val validFieldNames = validateFields.map { it.fieldName }.toSet()
//
//    for (field in data.keys) {
//        if (validFieldNames.contains(field)) {
//            updateQueue.add(field)
//        } else {
//            return Pair(updateQueue, false) // Return false if any invalid field is found
//        }
//    }
//
//    return Pair(updateQueue, true)
//}
//
//fun String.toJsonElementMap(): Map<String, JsonElement> {
//    val json = Json { isLenient = true }
//    val jsonElement = json.parseToJsonElement(this)
//    return jsonElement.jsonObject
//}
//
//fun mapToFiltersX(map: Map<String, JsonElement>): FiltersX? {
//    val (queue, isValid) = buildUpdateQueue(map, FiltersXValidateField.entries.toTypedArray())
//    if (!isValid) {
//        return null
//    }
//
//    val ids = map["ids"]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet()
//    val authors = map["authors"]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet()
//    val kinds = map["kinds"]?.jsonArray?.map { it.jsonPrimitive.int }?.toSet()
//    val since = map["since"]?.jsonPrimitive?.longOrNull
//    val until = map["until"]?.jsonPrimitive?.longOrNull
//    val limit = map["limit"]?.jsonPrimitive?.longOrNull
//    val search = map["search"]?.jsonPrimitive?.contentOrNull
//
//    return FiltersX(ids, authors, kinds, since, until, limit, search)
//}
//
//fun mapToEvent(map: Map<String, JsonElement>): Event? {
//    val (queue, isValid) = buildUpdateQueue(map, EventValidateField.entries.toTypedArray())
//    if (!isValid) {
//        return null
//    }
//
//    val id = map["id"]?.jsonPrimitive?.contentOrNull
//    val pubkey = map["pubkey"]?.jsonPrimitive?.contentOrNull
//    val createAt = map["created_at"]?.jsonPrimitive?.longOrNull
//    val content = map["content"]?.jsonPrimitive?.contentOrNull
//    val kind = map["kind"]?.jsonPrimitive?.intOrNull
//    val tags = map["tags"]?.jsonArray?.map { it.jsonArray.map { tag -> tag.jsonPrimitive.content } }
//    val signature = map["sig"]?.jsonPrimitive?.contentOrNull
//
//    return Event(id, pubkey, createAt, content, kind, tags, signature)
//}

/*
fun main() {
    val jsonString1 = """
        {"ids": ["e4b2c64f0e4e54abb34d5624cd040e05ecc77f0c467cc46e2cc4d5be98abe3e3"], "kinds": [4]}
    """.trimIndent()
    val jsonString2 = """
        {"search": "purple", "kinds": [1], "status": true, "since": 1715181359}
    """.trimIndent()
    val jsonString3 = """
        {
          "id": "000006d8c378af1779d2feebc7603a125d99eca0ccf1085959b307f64e5dd358",
          "pubkey": "a48380f4cfcc1ad5378294fcac36439770f9c878dd880ffa94bb74ea54a6f243",
          "created_at": 1651794653,
          "kind": 1,
          "tags": [
            ["nonce", "776797", "21"]
          ],
          "content": "It's just me mining my own business",
          "sig": "284622fc0a3f4f1303455d5175f7ba962a3300d136085b9566801bc2e0699de0c7e31e44c81fb40ad9049173742e904713c3594a1da0fc5d2382a25c11aba977"
        }
    """.trimIndent()

    val map1: Map<String, JsonElement> = jsonString1.toJsonElementMap()
    val map2: Map<String, JsonElement> = jsonString2.toJsonElementMap()
    val map3: Map<String, JsonElement> = jsonString3.toJsonElementMap()

    val filtersX1 = mapToFiltersX(map1)
    val filtersX2 = mapToFiltersX(map2)
    val event = mapToEvent(map3)

    if (filtersX1 != null) {
        println("FiltersX 1: $filtersX1")
    } else {
        println("FiltersX 1: Error: Invalid fields detected.")
    }

    if (filtersX2 != null) {
        println("FiltersX 2: $filtersX2")
    } else {
        println("FiltersX 2: Error: Invalid fields detected.")
    }

    if (event != null) {
        println("Event: $event")

        event.tags?.forEach { tagList ->
            tagList.forEachIndexed { index, tag ->
                println("[$index] $tag")
            }
        }
    } else {
        println("Event: Error: Invalid fields detected.")
    }
}
 */
