package com.group.services.vk

fun parseCommand(payload: String): Commands {
    val action = payload
        .substringAfter(":")
        .substringAfter("\"")
        .substringBefore("\"")
    return Commands.valueOf(action.toUpperCase())
}