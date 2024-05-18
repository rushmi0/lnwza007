package org.lnwza007.database.service

import org.lnwza007.infra.database.tables.Event


interface EventService {


    suspend fun addEvent(event: Event)

    suspend fun deleteEvent(event: Event)

    suspend fun selectEventByPOW()



}

