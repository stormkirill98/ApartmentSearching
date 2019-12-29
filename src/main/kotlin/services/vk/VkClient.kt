package com.group.services.vk

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.group.services.vk.enums.EventType
import io.ktor.http.HttpStatusCode
import org.slf4j.LoggerFactory
import java.util.*

object VkClient {
    private val accessKey: String
    private val confirmedKey: String

    private val logger = LoggerFactory.getLogger(VkClient::class.java)

    init {
        val propFile = VkClient::class.java.getResource("/config.properties")
        val props = Properties()

        props.load(propFile.openStream())

        accessKey = props.getProperty("vk-access-key")
        confirmedKey = props.getProperty("vk-confirmed-key")
    }


    fun handleRequest(body: String): Pair<String, HttpStatusCode> {
        logger.debug(body)

        val event = Gson().fromJson(body, Event::class.java)
        logger.debug(event.toString())

        when(event.getEventType()){
            EventType.CONFIRMATION -> return Pair("18359792", HttpStatusCode.OK)
        }

        return Pair("ok", HttpStatusCode.OK)
    }
}

data class Event(val type: String = "",
                 val obj: String = "",
                 @SerializedName("group_id") val groupId: Int = 0,
                 @SerializedName("event_id") val eventId: String = "") {
    fun getEventType() = EventType.valueOf(type)
}