package com.group.parsing.flat

import com.group.emptyCalendar
import com.group.nowCalendar
import com.group.parsing.HOUR
import com.group.parsing.getDifferenceFromNow
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import parsing.flat.Flat
import java.text.SimpleDateFormat
import java.util.*

object AvitoParser {
    private val logger = LoggerFactory.getLogger(AvitoParser::class.java)

    suspend fun parseAsync(
        url: String,
        send: (flat: Flat) -> Unit
    ) {
        logger.info("Start parse avito")

        Jsoup.connect(url).get().run {
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

                coroutineScope {
                    val nameThread = async { getName(header) }
                    val flatUrlThread = async { getUrl(header) }
                    val priceThread = async { getPrice(header) }
                    val addressThread = async { getAddress(el) }
                    val imagesThread = async { getImages(el) }

                    launch {
                        logger.info("[${Thread.currentThread().name}] launch send flat from avito")

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

        logger.info("End parse avito")
    }

    private fun getName(div: Element) = div.select("h3.snippet-title")[0].text()

    private fun getDate(div: Element): Calendar {
        val dateDiv = div.select("div.js-item-date")
        if (dateDiv.isNullOrEmpty()) return emptyCalendar()

        val dateStr = dateDiv.get(0).attr("data-absolute-date")
        if (dateStr.isNullOrBlank()) return emptyCalendar()

        return com.group.parsing.getDate(dateStr)
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
