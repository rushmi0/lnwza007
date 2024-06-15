package org.lnwza007.database.service

import org.lnwza007.relay.modules.Event
import org.lnwza007.relay.modules.FiltersX


interface EventService {

    suspend fun saveEvent(event: Event) : Boolean

    suspend fun deleteEvent(eventId: String) : Boolean

    suspend fun selectById(id: String): Event?

    suspend fun filterList(filters: FiltersX): List<Event>

}