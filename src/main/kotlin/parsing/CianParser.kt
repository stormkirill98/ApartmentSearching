package com.group.parsing

import com.group.services.vk.VkApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.util.*

object CianParser {
    fun parse(
        url: String,
        send: (flat: Flat) -> Unit
    ) {
        Jsoup.connect(url).get().run {
            for (el in select("div.c6e8ba5398--main-container--1FMpY")) {
                val imagesDiv = el.select("div.c6e8ba5398--media--HK4H1").last() ?: continue
                val infoDiv = el.select("div.c6e8ba5398--main--1NDwp").last() ?: continue

                val dateThread = GlobalScope.async { getDate(infoDiv) }
                val flatUrlThread = GlobalScope.async { getUrl(infoDiv) }
                val nameThread = GlobalScope.async { getName(infoDiv) }
                val priceThread = GlobalScope.async { getPrice(infoDiv) }
                val addressThread = GlobalScope.async { getAddress(infoDiv) }
                val imageUrlsThread = GlobalScope.async { getImageUrls(imagesDiv) }

                GlobalScope.launch {
                    send(
                        Flat(
                            nameThread.await(),
                            dateThread.await(),
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

    private fun getUrl(infoDiv: Element): String {
        return infoDiv.select("a.c6e8ba5398--header--1fV2A").last().attr("href")
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