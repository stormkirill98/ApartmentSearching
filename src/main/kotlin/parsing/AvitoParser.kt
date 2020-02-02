package com.group.parsing

import com.group.nowCalendar
import com.group.nowDate
import com.group.services.vk.VkApi
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

private const val HOUR = 3_600_000

object AvitoParser {
    private val logger = LoggerFactory.getLogger(AvitoParser::class.java)

    fun parse(url: String) {
        Jsoup.connect(url).get().run {
            for (el in select("div.item__line")) {
                val date = getDate(el)
                val dateDifference = nowDate().time - date.timeInMillis
                val isRaised = isRaised(el)

                // пропускаем квартиры расположенные вверху списка, из-за того что их подняли
                if (dateDifference > 5 * HOUR && isRaised)
                    continue

                // прекращаем смотреть квартиры, как встречаем квартиру с давним временем и не поднятую
                if (dateDifference > 5 * HOUR && !isRaised)
                    break

                val header = el.select("div.item_table-header")[0]

                val name = getName(header)
                val flatUrl = getUrl(header)
                val price = getPrice(header)
                val address = getAddress(el)
                val images = getImages(el)

                // TODO: send async
                VkApi.sendApartment(name, date, flatUrl, price, address, images)
            }
        }
    }

    private fun getName(div: Element) = div.select("h3.snippet-title")[0].text()

    private fun getDate(div: Element): Calendar {
        fun changeTime(s: String, date: Calendar) {
            val nums = s.split(":")

            date.set(Calendar.HOUR_OF_DAY, nums[0].toInt())
            date.set(Calendar.MINUTE, nums[1].toInt())
        }

        val dateDiv = div.select("div.js-item-date")
        val dateStr = dateDiv.get(0).attr("data-absolute-date")
        val date = nowCalendar()

        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm Z")
        logger.info("Now date: ${formatter.format(date.time)}, flat date: $dateStr")

        when {
            dateStr.contains("Сегодня") -> {
                changeTime(dateStr.substringAfter(" "), date)
            }

            dateStr.contains("Вчера") -> {
                date.add(Calendar.DAY_OF_MONTH, -1)
                changeTime(dateStr.substringAfter(" "), date)
            }

            else -> date.clear()
        }

        logger.info("Date after parse: ${formatter.format(date.time)}")

        return date
    }

    private fun isRaised(div: Element) = div.select("div.vas-applied_bottom").size > 0

    private fun getUrl(div: Element): String {
        return "https://www.avito.ru" + div.select("a.snippet-link")[0].attr("href")
    }

    private fun getPrice(div: Element) = div.select("span.price").text().trim().substringBefore("₽")

    private fun getAddress(div: Element) = div.select("div.address")[0].text()

    private fun getImages(div: Element): List<String> {
        val imageDivs = div.select("a")
        val imageURLs = arrayListOf<String>()

        imageDivs.select("img[srcset]").forEach {
            imageURLs.add(it.attr("src").replace("208x156", "1280x960"))
        }

        return imageURLs
    }
}
