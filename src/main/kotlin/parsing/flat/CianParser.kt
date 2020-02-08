package com.group.parsing.flat

import com.group.parsing.HOUR
import com.group.parsing.getDifferenceFromNow
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory
import parsing.flat.Flat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

object CianParser {
    private val logger = LoggerFactory.getLogger(CianParser::class.java.name)

    suspend fun parseAsync(
        url: String,
        send: (flat: Flat) -> Unit
    ) {
        logger.info("Start parse cian")

        Jsoup.connect(url).get().run {
            for (el in select("div.c6e8ba5398--main-container--1FMpY")) {
                val infoDiv = el.select("div.c6e8ba5398--main--1NDwp").last() ?: continue

                val date = getDate(el)
                if (getDifferenceFromNow(date) > HOUR) {
                    logger.info("Break on flat: ${SimpleDateFormat("dd-MM HH:mm").format(date.time)}")
                    break
                }

                delay(Random.nextLong(2000, 6000))

                val flatUrl = getUrl(infoDiv)

                coroutineScope {
                    val flatCoroutine = async { parseFlat(flatUrl, date) }

                    launch {
                        logger.info("[${Thread.currentThread().name}] launch send flat from cian")

                        send(flatCoroutine.await())
                    }
                }
            }
        }

        logger.info("End parse cian")
    }

    private suspend fun parseFlat(url: String, date: Calendar): Flat {
        Jsoup.connect(url).get().run {
            return coroutineScope {
                val imageUrlsThread = async { getImageUrlsAsync(this@run) }
                val nameThread = async { getName(this@run) }
                val priceThread = async { getPrice(this@run) }
                val addressThread = async { getAddress(this@run) }
                val descriptionThread = async { getDescription(this@run) }

                return@coroutineScope Flat(
                    nameThread.await(),
                    date,
                    url,
                    priceThread.await(),
                    addressThread.await(),
                    descriptionThread.await(),
                    imageUrlsThread.await()
                )
            }
        }
    }

    private fun getDescription(div: Element): String {
        return div.select("p.a10a3f92e9--description-text--1_Lup").last().text()
    }

    private fun getUrl(div: Element): String {
        return div.select("a.c6e8ba5398--header--1fV2A").last().attr("href")
    }

    private fun getDate(div: Element): Calendar {
        val dateStr = div.select("div.c6e8ba5398--absolute--9uFLj").last().text()
        return com.group.parsing.getDate(dateStr)
    }

    private fun getName(div: Element): String {
        return div.select("h1.a10a3f92e9--title--2Widg").last().text()
    }

    private fun getPrice(div: Element): String {
        return div.select("span.a10a3f92e9--price_value--1iPpd").last().text().substringBefore("₽").trim()
    }

    private fun getAddress(div: Element): String {
        return div.select("address.a10a3f92e9--address--140Ec").last().text()
            .substringAfter("Ярославль,")
    }

    private fun getImageUrlsAsync(div: Element): List<String> {
        val imageUrls = arrayListOf<String>()

        for(imageDiv in div.select("div.fotorama__nav__frame")) {
            imageUrls.add(imageDiv.attr("src").replace("2.jpg", "1.jpg"))
        }

        return imageUrls
    }
}