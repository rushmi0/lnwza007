package org.lnwza007.relay.service.nip01

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.inject.Singleton
import kotlinx.serialization.json.*
import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.FiltersX
import org.lnwza007.relay.modules.TagElt
import org.slf4j.LoggerFactory

@Singleton
object Transform : VerificationFactory() {

    private val LOG = LoggerFactory.getLogger(Transform::class.java)

    private fun Map<String, JsonElement>.toTagMap(): Map<TagElt, Set<String>> {
        return this.filterKeys { it.startsWith("#") }
            .mapKeys { (key, _) -> TagElt.valueOf(key.removePrefix("#")) }
            .mapValues { (_, value) ->
                value.jsonArray.mapNotNull { it.jsonPrimitive.contentOrNull }.toSet()
            }
    }

    fun convertToFiltersXObject(field: Map<String, JsonElement>): FiltersX {
        return FiltersX(
            ids = field["ids"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }?.toSet() ?: emptySet(),
            authors = field["authors"]?.jsonArray?.mapNotNull { it.jsonPrimitive.contentOrNull }?.toSet() ?: emptySet(),
            kinds = field["kinds"]?.jsonArray?.mapNotNull { it.jsonPrimitive.long }?.toSet() ?: emptySet(),
            tags = field.toTagMap(),
            since = field["since"]?.jsonPrimitive?.longOrNull,
            until = field["until"]?.jsonPrimitive?.longOrNull,
            limit = field["limit"]?.jsonPrimitive?.longOrNull,
            search = field["search"]?.jsonPrimitive?.contentOrNull
        )
    }


    fun convertToEventObject(field: Map<String, JsonElement>): Event {
        return Event(
            id = field["id"]?.jsonPrimitive?.contentOrNull,
            pubKey = field["pubkey"]?.jsonPrimitive?.contentOrNull,
            createAt = field["created_at"]?.jsonPrimitive?.longOrNull,
            kind = field["kind"]?.jsonPrimitive?.longOrNull,
            tags = field["tags"]?.jsonArray?.map { it.jsonArray.map { tag -> tag.jsonPrimitive.content } },
            content = field["content"]?.jsonPrimitive?.contentOrNull,
            signature = field["sig"]?.jsonPrimitive?.contentOrNull
        )
    }

    /**
     * ฟังก์ชัน toJsonString ใช้ในการแปลงข้อมูลใดๆเป็นสตริง JSON
     * @return สตริง JSON ที่เป็นผลลัพธ์จากการแปลง Object
     */
    fun Any.toJsonString(): String {
        return jacksonObjectMapper().writeValueAsString(this)
    }

    /**
     * ฟังก์ชัน toJsonElementMap ใช้ในการแปลงสตริง JSON เป็น Map ของ JsonElement
     * @return Map ของ JsonElement ที่เป็นผลลัพธ์จากการแปลงสตริง JSON
     */
    fun String.toJsonElementMap(): Map<String, JsonElement> {
        val option = Json { isLenient = true }
        val jsonElement = option.parseToJsonElement(this)
        return jsonElement.jsonObject
    }

    fun Map<String, JsonElement>.toFiltersX(): FiltersX {
        return convertToFiltersXObject(this)
    }

    fun Map<String, JsonElement>.toEvent(): Event {
        return convertToEventObject(this)
    }

    fun JsonObject.toFiltersX(): FiltersX {
        val fieldMap = this.toMap()
        return convertToFiltersXObject(fieldMap)
    }

    fun JsonObject.toEvent(): Event {
        val fieldMap = this.toMap()
        return convertToEventObject(fieldMap)
    }


    /*
    fun JsonObject.toFiltersX(): FiltersX {
        return Json.decodeFromJsonElement<FiltersX>(this)
    }

    fun JsonObject.toEvent(): Event {
        return Json.decodeFromJsonElement<Event>(this)
    }
     */

}
