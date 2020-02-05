package com.group.parsing

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.util.*

// TODO: move class Flat
data class Flat(
    val name: String,
    val date: Calendar,
    val url: String,
    val price: String,
    val address: String,
    val images: List<String>
)

object AvitoParser {
    private val logger = LoggerFactory.getLogger(AvitoParser::class.java)

    fun parse(
        url: String,
        send: (flat: Flat) -> Unit
    ) {
        Jsoup.connect(url).get().run {
            // TODO: try to run coroutines for each div with flat
            for (el in select("div.item__line")) {
                val date = getDate(el)
                val dateDifference = getDifferenceFromNow(date)
                val isRaised = isRaised(el)

                val formatter = SimpleDateFormat("dd-MM HH:mm")
                // пропускаем квартиры расположенные вверху списка, из-за того что их подняли
                if (dateDifference > HOUR && isRaised) {
                    logger.info("Skip flat: ${formatter.format(date.time)}")
                    continue
                }

                // прекращаем смотреть квартиры, как встречаем квартиру с давним временем и не поднятую
                if (dateDifference > HOUR && !isRaised) {
                    logger.info("Break search on flat: ${formatter.format(date.time)}")
                    break
                }

                val header = el.select("div.item_table-header")[0]

                val nameThread = GlobalScope.async { getName(header) }
                val flatUrlThread = GlobalScope.async { getUrl(header) }
                val priceThread = GlobalScope.async { getPrice(header) }
                val addressThread = GlobalScope.async { getAddress(el) }
                val imagesThread = GlobalScope.async { getImages(el) }

                GlobalScope.launch {
                    send(
                        Flat(
                            nameThread.await(),
                            date,
                            flatUrlThread.await(),
                            priceThread.await(),
                            addressThread.await(),
                            imagesThread.await()
                        )
                    )
                }
            }
        }
    }

    private fun getName(div: Element) = div.select("h3.snippet-title")[0].text()

    private fun getDate(div: Element): Calendar {
        val dateDiv = div.select("div.js-item-date")
        val dateStr = dateDiv.get(0).attr("data-absolute-date")

        return getDate(dateStr)
    }

    private fun isRaised(div: Element): Boolean {
        val childDiv = div.select("div.js-vas-list-container").last()
        childDiv ?: return false

        val dataProps = childDiv.attr("data-props")
        dataProps ?: return false

        return dataProps != "{\"vas\":[]}"
    }

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
