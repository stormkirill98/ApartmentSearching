package com.group.services.vk.enums

enum class EventType(val value: String) {
    MESSAGE_NEW("message_new"),
    MESSAGE_EDIT("message_edit"),
    MESSAGE_ALLOW("message_allow"),
    MESSAGE_DENY("message_deny"),

    GROUP_JOIN("group_join"),
    GROUP_LEAVE("group_leave")

    // Not all events, only require, need other add for the future
}