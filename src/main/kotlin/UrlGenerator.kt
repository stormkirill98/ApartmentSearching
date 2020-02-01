package com.group

import com.group.database.FlatSearchParameters
import com.ibm.icu.text.Transliterator

object UrlGenerator {
    fun getAvitoUrl(flatParameters: FlatSearchParameters): String {
        val url = StringBuilder("https://www.avito.ru/")

        val city = transliterateCyrillicToLatin(flatParameters.city.trim())
        url.append("$city/kvartiry/sdam/na_dlitelnyy_srok?s=104") // s = 104 - порядок по дате

        val district = flatParameters.districts.dropLast(1)
        if (district.isNotEmpty())
            url.append("&district=$district")

        val rooms = getRoomAvitoUrlPart(flatParameters.rooms)
        if (rooms.isNotEmpty())
            url.append("&$rooms")

        val startPrice = flatParameters.startPrice
        if(startPrice > 0)
            url.append("&pmin=$startPrice")

        val endPrice = flatParameters.endPrice
        if(endPrice > 0)
            url.append("&pmax=$endPrice")

        if (flatParameters.onlyOwner)
            url.append("&user=1")

        return url.toString()
    }

    private fun getRoomAvitoUrlPart(rooms: String): String {
        if (rooms.isEmpty()) return ""

        val oneRoom = rooms.contains("1,")
        val twoRoom = rooms.contains("2,")
        val threeRoom = rooms.contains("3,")
        val moreThreeRoom = rooms.contains("3+")

        return when {
            oneRoom && !twoRoom && !threeRoom && !moreThreeRoom -> "f=550_5702-5703"
            !oneRoom && twoRoom && !threeRoom && !moreThreeRoom -> "f=550_5704"
            !oneRoom && !twoRoom && threeRoom && !moreThreeRoom -> "f=550_5705"
            !oneRoom && !twoRoom && !threeRoom && moreThreeRoom -> "f=550_5706-5707-5708-11022-11023-11024-11025"
            oneRoom && twoRoom && !threeRoom && !moreThreeRoom -> "f=550_5702-5703-5704"
            oneRoom && !twoRoom && threeRoom && !moreThreeRoom -> "f=550_5702-5703-5705"
            !oneRoom && twoRoom && threeRoom && !moreThreeRoom -> "f=550_5704-5705"
            oneRoom && twoRoom && threeRoom && !moreThreeRoom -> "f=550_5702-5703-5704-5705"
            oneRoom && twoRoom && threeRoom && moreThreeRoom -> "f=550_5702-5703-5704-5705-5706-5707-5708-11022-11023-11024-11025"
            else -> ""
        }
    }
}

fun transliterateCyrillicToLatin(str: String): String {
    val toLatinTrans = Transliterator.getInstance("Russian-Latin/BGN")
    return toLatinTrans.transliterate(str).replace("ʹ", "")
}