package org.lnwza007.relay.service.nip01

import jakarta.inject.Singleton
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.FiltersX
import org.lnwza007.relay.modules.TagElement
import org.slf4j.LoggerFactory

@Singleton
object Transform : VerificationFactory() {

    private val LOG = LoggerFactory.getLogger(Transform::class.java)

    private fun Map<String, JsonElement>.toTagMap(): Map<TagElement, Set<String>> {
        return this.filterKeys { it.startsWith("#") }
            .mapKeys { (key, _) -> TagElement.valueOf(key.removePrefix("#")) }
            .mapValues { (_, value) ->
                value.jsonArray.mapNotNull { it.jsonPrimitive.contentOrNull }.toSet()
            }
    }


    fun convertToFiltersXObject(field: Map<String, JsonElement>): FiltersX {
        /*
        val tags: Map<TagElement, Set<String>> = field.keys
            .filter { it.startsWith("#") } // เลือกเฉพาะ key ที่ขึ้นต้นด้วย #
            .associateWith { tag ->
                field[tag]?.jsonArray?.mapNotNull {
                    it.jsonPrimitive.contentOrNull
                }?.toSet() ?: emptySet()
            }
            .mapKeys { (key, _) ->
                TagElement.valueOf(key.removePrefix("#"))
            }
         */

        val tags: Map<TagElement, Set<String>> = field.toTagMap()
        return FiltersX(
            ids = field["ids"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }?.toSet() ?: emptySet(),
            authors = field["authors"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }?.toSet() ?: emptySet(),
            kinds = field["kinds"]?.jsonArray?.mapNotNull { it.jsonPrimitive.long }?.toSet() ?: emptySet(),
            tags = tags,
            since = field["since"]?.jsonPrimitive?.longOrNull,
            until = field["until"]?.jsonPrimitive?.longOrNull,
            limit = field["limit"]?.jsonPrimitive?.longOrNull,
            search = field["search"]?.jsonPrimitive?.contentOrNull
        )
    }


    fun convertToEventObject(field: Map<String, JsonElement>): Event {
        return Event(
            id = field["id"]?.jsonPrimitive?.contentOrNull,
            pubkey = field["pubkey"]?.jsonPrimitive?.contentOrNull,
            createAt = field["created_at"]?.jsonPrimitive?.longOrNull,
            content = field["content"]?.jsonPrimitive?.contentOrNull,
            kind = field["kind"]?.jsonPrimitive?.longOrNull,
            tags = field["tags"]?.jsonArray?.map { it.jsonArray.map { tag -> tag.jsonPrimitive.content } },
            signature = field["sig"]?.jsonPrimitive?.contentOrNull
        )
    }

    fun Map<String, JsonElement>.toFiltersX(): FiltersX {
        return convertToFiltersXObject(this)
    }

    fun Map<String, JsonElement>.toEvent(): Event {
        return convertToEventObject(this)
    }


    fun JsonObject.toFiltersX(): FiltersX {
        return Json.decodeFromJsonElement<FiltersX>(this)
    }

    fun JsonObject.toEvent(): Event {
        return Json.decodeFromJsonElement<Event>(this)
    }

}
