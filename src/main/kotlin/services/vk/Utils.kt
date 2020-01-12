package com.group.services.vk

import com.group.services.vk.enums.Commands

fun parseCommand(payload: String): Commands {
    val action = payload
        .substringAfter(":")
        .substringAfter("\"")
        .substringBefore("\"")
    return Commands.valueOf(action.toUpperCase())
}