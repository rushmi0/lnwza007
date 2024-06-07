package org.lnwza007.relay.service.nip01

import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.FiltersX
import org.slf4j.LoggerFactory

@Singleton
object Transform : VerificationFactory() {

    private val LOG = LoggerFactory.getLogger(Transform::class.java)

    fun convertToFiltersXObject(field: Map<String, JsonElement>): FiltersX {
        return FiltersX(
            ids = field["ids"]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet(),
            authors = field["authors"]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet(),
            kinds = field["kinds"]?.jsonArray?.map { it.jsonPrimitive.long }?.toSet(),
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
