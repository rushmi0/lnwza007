package org.lnwza007.relay.service.nip01

import jakarta.inject.Singleton
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.EventValidateField
import org.lnwza007.relay.modules.FiltersX
import org.lnwza007.relay.modules.FiltersXValidateField

@Singleton
object Transform : ValidateField() {

    private fun convertToFiltersX(field: Map<String, JsonElement>): FiltersX {
        return FiltersX(
            ids = field["ids"]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet(),
            authors = field["authors"]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet(),
            kinds = field["kinds"]?.jsonArray?.map { it.jsonPrimitive.int }?.toSet(),
            since = field["since"]?.jsonPrimitive?.longOrNull,
            until = field["until"]?.jsonPrimitive?.longOrNull,
            limit = field["limit"]?.jsonPrimitive?.longOrNull,
            search = field["search"]?.jsonPrimitive?.contentOrNull
        )
    }

    private fun convertToEvent(field: Map<String, JsonElement>): Event {
        return Event(
            id = field["id"]?.jsonPrimitive?.contentOrNull,
            pubkey = field["pubkey"]?.jsonPrimitive?.contentOrNull,
            createAt = field["created_at"]?.jsonPrimitive?.longOrNull,
            content = field["content"]?.jsonPrimitive?.contentOrNull,
            kind = field["kind"]?.jsonPrimitive?.intOrNull,
            tags = field["tags"]?.jsonArray?.map { it.jsonArray.map { tag -> tag.jsonPrimitive.content } },
            signature = field["sig"]?.jsonPrimitive?.contentOrNull
        )
    }

    fun Map<String, JsonElement>.toFiltersX(): FiltersX? {
        return mapToObject(this, FiltersXValidateField.entries.toTypedArray(), ::convertToFiltersX)
    }

    fun Map<String, JsonElement>.toEvent(): Event? {
        return mapToObject(this, EventValidateField.entries.toTypedArray(), ::convertToEvent)
    }

}
