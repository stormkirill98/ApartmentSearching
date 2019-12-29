package com.group.services.vk.entities

import com.google.gson.annotations.SerializedName
import com.group.services.vk.enums.EventType

data class Event(
    val type: String = "",
    val obj: String = "",
    @SerializedName("group_id") val groupId: Int = 0,
    @SerializedName("event_id") val eventId: String = ""
) {
    fun getEventType() = EventType.valueOf(type.toUpperCase())
}