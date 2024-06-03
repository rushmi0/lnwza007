package org.lnwza007.relay.service.nip01

import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.EventValidateField
import org.lnwza007.relay.modules.FiltersX
import org.lnwza007.relay.modules.FiltersXValidateField
import org.slf4j.LoggerFactory

@Singleton
object Transform : VerificationFactory() {

    private val LOG = LoggerFactory.getLogger(Transform::class.java)

    private fun convertToFiltersXObject(field: Map<String, JsonElement>): FiltersX {
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

    private fun convertToEventObject(field: Map<String, JsonElement>): Event {
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


    fun Map<String, JsonElement>.toFiltersX(): Flow<Pair<String?, FiltersX?>> = flow {
        mapToObject(
            this@toFiltersX,
            FiltersXValidateField.entries.toTypedArray(),
            ::convertToFiltersXObject
        ).collect { (status, message, obj) ->
            if (status) {
                LOG.info("FiltersX conversion successful")
                emit(Pair(message, obj))
            } else {
                LOG.warn("FiltersX conversion failed")
                emit(Pair(message, null))
            }
        }
    }

    fun Map<String, JsonElement>.toEvent(): Flow<Pair<String?, Event?>> = flow {
        mapToObject(
            this@toEvent,
            EventValidateField.entries.toTypedArray(),
            ::convertToEventObject
        ).collect { (status, message, obj) ->
            if (status) {
                LOG.info("Event conversion successful")
                emit(Pair(message, obj))
            } else {
                LOG.warn("Event conversion failed")
                emit(Pair(message, null))
            }
        }
    }

}
