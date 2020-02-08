package com.group.parsing.flat

import com.group.parsing.HOUR
import com.group.parsing.getDifferenceFromNow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import parsing.flat.Flat
import java.text.SimpleDateFormat
import java.util.*

object CianParser {
    private val logger = LoggerFactory.getLogger(CianParser::class.java.name)

    suspend fun parseAsync(
        url: String,
        send: (flat: Flat) -> Unit
    ) {
        logger.info("Start parse cian")

        Jsoup.connect(url).get().run {
            for (el in select("div.c6e8ba5398--main-container--1FMpY")) {
                val imagesDiv = el.select("div.c6e8ba5398--media--HK4H1").last() ?: continue
                val infoDiv = el.select("div.c6e8ba5398--main--1NDwp").last() ?: continue

                val date = getDate(el)

                if (getDifferenceFromNow(date) > HOUR) {
                    logger.info("Break on flat: ${SimpleDateFormat("dd-MM HH:mm").format(date.time)}")
                    break
                }

                coroutineScope {
                    val flatUrlThread = async { getUrl(infoDiv) }
                    val nameThread = async { getName(infoDiv) }
                    val priceThread = async { getPrice(infoDiv) }
                    val addressThread = async { getAddress(infoDiv) }
                    val imageUrlsThread = async { getImageUrls(imagesDiv) }

                    launch {
                        logger.info("[${Thread.currentThread().name}] launch send flat from cian")

                        send(
                            Flat(
                                nameThread.await(),
                                date,
                                flatUrlThread.await(),
                                priceThread.await(),
                                addressThread.await(),
                                imageUrlsThread.await()
                            )
                        )
                    }
                }
            }
        }

        logger.info("End parse cian")
    }

    private fun getUrl(infoDiv: Element): String {
        return infoDiv.select("a.c6e8ba5398--header--1fV2A").last().attr("href")
    }

    private fun getDate(infoDiv: Element): Calendar {
        val dateStr = infoDiv.select("div.c6e8ba5398--absolute--9uFLj").last().text()
        return com.group.parsing.getDate(dateStr)
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