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
        if (startPrice > 0)
            url.append("&pmin=$startPrice")

        val endPrice = flatParameters.endPrice
        if (endPrice > 0)
            url.append("&pmax=$endPrice")

        if (flatParameters.onlyOwner)
            url.append("&user=1")

        return url.toString()
    }

    fun getCianUrl(flatParameters: FlatSearchParameters): String {
        val url =
            StringBuilder("https://yaroslavl.cian.ru/cat.php?deal_type=rent&engine_version=2&region=5075&offer_type=flat&totime=3600")

        if (flatParameters.onlyOwner)
            url.append("&is_by_homeowner=1")

        if (flatParameters.startPrice != 0)
            url.append("&minprice=${flatParameters.startPrice}")

        if (flatParameters.endPrice != 0)
            url.append("&maxprice=${flatParameters.endPrice}")

        url.append(getRoomCianUrlPart(flatParameters.rooms))
        url.append(getDistrictCianUrlPath(flatParameters.districts))

        return url.toString()
    }

    private fun getRoomCianUrlPart(rooms: String): String {
        if (rooms.isEmpty()) return ""
        val roomPartUrl = StringBuilder("")

        if (rooms.contains("1,"))
            roomPartUrl.append("&room1=1&room9=1")

        if (rooms.contains("2,"))
            roomPartUrl.append("&room2=1")

        if (rooms.contains("3,"))
            roomPartUrl.append("&room3=1")

        if (rooms.contains("3+"))
            roomPartUrl.append("&&room4=1&room5=1&room6=1")

        return roomPartUrl.toString()
    }

    private fun getDistrictCianUrlPath(districts: String): String {
        if (districts.isEmpty()) return ""
        val districtPathUrl = java.lang.StringBuilder("")

        if (districts.contains("172"))
            districtPathUrl.append("&district%5B0%5D=319")

        if (districts.contains("174"))
            districtPathUrl.append("&district%5B0%5D=321")

        if (districts.contains("176"))
            districtPathUrl.append("&district%5B0%5D=323")

        if (districts.contains("177"))
            districtPathUrl.append("&district%5B0%5D=324")

        if (districts.contains("173"))
            districtPathUrl.append("&district%5B0%5D=320")

        if (districts.contains("175"))
            districtPathUrl.append("&district%5B0%5D=322")

        return districtPathUrl.toString()
    }

    private fun getRoomAvitoUrlPart(rooms: String): String {
        if (rooms.isEmpty()) return ""

        val oneRoom = rooms.contains("1,")
        val twoRoom = rooms.contains("2,")
        val threeRoom = rooms.contains("3,")
        val moreThreeRoom = rooms.contains("3+")

        // TODO: can to improve
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