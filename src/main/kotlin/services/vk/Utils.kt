package com.group.services.vk

import com.group.services.vk.enums.Commands
import com.ibm.icu.text.Transliterator

fun parseCommand(payload: String): Commands {
    val action = payload
        .substringAfter(":")
        .substringAfter("\"")
        .substringBefore("\"")
    return Commands.valueOf(action.toUpperCase())
}

fun transliterateCyrillicToLatin(str: String): String {
    val toLatinTrans = Transliterator.getInstance("Russian-Latin/BGN")
    return toLatinTrans.transliterate(str).replace("ʹ", "")
}