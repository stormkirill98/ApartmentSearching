package com.group.services.vk

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.group.services.getProperty
import com.group.services.vk.enums.EventType
import io.ktor.http.HttpStatusCode
import org.slf4j.LoggerFactory

object VkClient {
    private val confirmedKey = getProperty("vk-confirmed-key")

    private val logger = LoggerFactory.getLogger(VkClient::class.java)


    fun handleRequest(body: String): Pair<String, HttpStatusCode> {
        logger.debug(body)

        val event = Gson().fromJson(body, Event::class.java)
        logger.debug(event.toString())

        when (event.getEventType()) {
            EventType.CONFIRMATION -> return Pair(confirmedKey, HttpStatusCode.OK)
            EventType.GROUP_JOIN -> handleGroupJoin(event)
        }

        return Pair("ok", HttpStatusCode.OK)
    }

    private fun handleGroupJoin(event: Event) {

    }
}

data class Event(
    val type: String = "",
    val obj: String = "",
    @SerializedName("group_id") val groupId: Int = 0,
    @SerializedName("event_id") val eventId: String = ""
) {
    fun getEventType() = EventType.valueOf(type.toUpperCase())
}