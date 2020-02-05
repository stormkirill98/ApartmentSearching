package com.group.parsing

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.*

fun main() {
    CianParser.parse("https://yaroslavl.cian.ru/cat.php?deal_type=rent&engine_version=2&offer_type=flat&region=5075&room1=1&room2=1&room3=1&totime=3600&type=4")
}

object CianParser {
    fun parse(url: String) {
        Jsoup.connect(url).get().run {
            for (el in select("div.c6e8ba5398--main-container--1FMpY")) {
                val imagesDiv = el.select("div.c6e8ba5398--media--HK4H1").last()
                val infoDiv = el.select("div.c6e8ba5398--main--1NDwp").last()

                val date = getDate(infoDiv)
                val dateDifference = getDifferenceFromNow(date)

                if (dateDifference > HOUR)
                    continue

                val name = getName(infoDiv)
                val price = getPrice(infoDiv)
                val address = getAddress(infoDiv)
                val imageUrls = getImageUrls(imagesDiv)

                println("Flat: $name $price $address $imageUrls")
            }
        }
    }

    private fun getDate(infoDiv: Element): Calendar {
        val dateStr = infoDiv.select("div.c6e8ba5398--absolute--9uFLj").last().text()
        return getDate(dateStr)
    }

    private fun getName(infoDiv: Element): String {
        return infoDiv.select("div.c6e8ba5398--title--2CW78").last().text()
    }

    private fun getPrice(infoDiv: Element): String {
        return infoDiv.select("div.c6e8ba5398--header--1df-X").last().text().substringBefore("₽").trim()
    }

    private fun getAddress(infoDiv: Element): String {
        return infoDiv.select("div.c6e8ba5398--address-links--1tfGW").last().text()
            .substringAfter("Ярославль,")
    }

    // TODO: need to improve
    private fun getImageUrls(imagesDiv: Element): List<String> {
        return imagesDiv.select("img").map { it.attr("src").replace("2.jpg", "1.jpg") }
    }
}