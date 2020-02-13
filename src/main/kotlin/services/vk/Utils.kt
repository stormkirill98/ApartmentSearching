package com.group.services.vk

import com.group.services.vk.enums.Command
import com.group.services.vk.enums.CountRoomCommand
import com.group.services.vk.enums.LandlordCommand

fun parseCommand(payload: String?): Command {
    if (payload == null) return Command.NONE

    val action = parsePayload(payload)

    return try {
        Command.valueOf(action)
    } catch (e: IllegalArgumentException) {
        Command.NONE
    }
}

fun parseLandlordCommand(payload: String?): LandlordCommand {
    if (payload == null) return LandlordCommand.NONE

    val action = parsePayload(payload)

    return try {
        LandlordCommand.valueOf(action)
    } catch (e: IllegalArgumentException) {
        LandlordCommand.NONE
    }
}

fun parseCountRoomCommand(payload: String?): CountRoomCommand {
    if (payload == null) return CountRoomCommand.NONE

    val action = parsePayload(payload)

    return try {
        CountRoomCommand.valueOf(action)
    } catch (e: IllegalArgumentException) {
        CountRoomCommand.NONE
    }
}

fun parseDistrict(payload: String) = payload.substringAfter("_").substringBefore("\"")

fun isPriceMsg(msg: String) = msg.matches(Regex("(от \\d+ до \\d+|от \\d+|до \\d+)"))

fun parsePrice(s: String): Pair<Int, Int> {
    val startPrice = if (s.contains("от "))
        s.substringAfter("от ").substringBefore(" до").toInt()
    else 0

    val endPrice = if (s.contains("до "))
        s.substringAfter("до ").toInt()
    else 0

    return startPrice to endPrice
}

private fun parsePayload(payload: String) = payload
    .substringAfter(":")
    .substringAfter("\"")
    .substringBefore("\"")