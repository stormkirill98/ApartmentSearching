package com.group.services.vk

import com.group.services.vk.enums.Command
import com.group.services.vk.enums.CountRoomCommand
import com.group.services.vk.enums.LandlordCommand

fun parseCommand(payload: String): Command {
    val action = payload
        .substringAfter(":")
        .substringAfter("\"")
        .substringBefore("\"")

    return try {
        // TODO: enhancement: remove toUpperCase(),
        //  all command in payload already in uppercase,
        //  not forget to change commands in Postman on uppercase
        Command.valueOf(action.toUpperCase())
    } catch (e: IllegalArgumentException) {
        Command.NONE
    }
}

fun parseLandlordCommand(payload: String): LandlordCommand {
    val action = payload
        .substringAfter(":")
        .substringAfter("\"")
        .substringBefore("\"")

    return try {
        LandlordCommand.valueOf(action.toUpperCase())
    } catch (e: IllegalArgumentException) {
        LandlordCommand.NONE
    }
}

fun parseCountRoomCommand(payload: String): CountRoomCommand {
    val action = payload
        .substringAfter(":")
        .substringAfter("\"")
        .substringBefore("\"")

    return try {
        CountRoomCommand.valueOf(action.toUpperCase())
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