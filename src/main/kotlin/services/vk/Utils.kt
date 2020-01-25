package com.group.services.vk

import com.group.services.vk.enums.Command
import com.group.services.vk.enums.CountRoomCommand
import com.group.services.vk.enums.LandlordCommand
import com.ibm.icu.text.Transliterator

fun parseCommand(payload: String): Command {
    val action = payload
        .substringAfter(":")
        .substringAfter("\"")
        .substringBefore("\"")

    return try {
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

fun parseDistrict(payload: String) = payload.substringAfter("_")

fun transliterateCyrillicToLatin(str: String): String {
    val toLatinTrans = Transliterator.getInstance("Russian-Latin/BGN")
    return toLatinTrans.transliterate(str).replace("ʹ", "")
}