package org.lnwza007.relay.service.nip01

import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.EventValidateField
import org.lnwza007.relay.modules.FiltersX
import org.lnwza007.relay.modules.FiltersXValidateField

object Transform : ValidateField() {


    fun Map<String, JsonElement>.toFiltersX(): FiltersX? {
        return mapToObject(this, FiltersXValidateField.entries.toTypedArray()) { field ->
            val ids = field["ids"]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet()
            val authors = field["authors"]?.jsonArray?.map { it.jsonPrimitive.content }?.toSet()
            val kinds = field["kinds"]?.jsonArray?.map { it.jsonPrimitive.int }?.toSet()
            val since = field["since"]?.jsonPrimitive?.longOrNull
            val until = field["until"]?.jsonPrimitive?.longOrNull
            val limit = field["limit"]?.jsonPrimitive?.longOrNull
            val search = field["search"]?.jsonPrimitive?.contentOrNull
            FiltersX(ids, authors, kinds, since, until, limit, search)
        }
    }

    fun Map<String, JsonElement>.toEvent(): Event? {
        return mapToObject(this, EventValidateField.entries.toTypedArray()) { field ->
            val id = field["id"]?.jsonPrimitive?.contentOrNull
            val pubkey = field["pubkey"]?.jsonPrimitive?.contentOrNull
            val createAt = field["created_at"]?.jsonPrimitive?.longOrNull
            val content = field["content"]?.jsonPrimitive?.contentOrNull
            val kind = field["kind"]?.jsonPrimitive?.intOrNull
            val tags = field["tags"]?.jsonArray?.map { it.jsonArray.map { tag -> tag.jsonPrimitive.content } }
            val signature = field["sig"]?.jsonPrimitive?.contentOrNull
            Event(id, pubkey, createAt, content, kind, tags, signature)
        }
    }

}