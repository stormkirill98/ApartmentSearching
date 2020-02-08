package com.group

import com.group.database.User
import com.group.parsing.flat.AvitoParser
import com.group.parsing.flat.CianParser
import com.group.services.vk.VkApi
import com.group.services.vk.VkClient
import com.group.services.vk.VkMsgApi
import com.group.tasks.SearchFlatServlet
import com.group.tasks.runSearchFlatTask
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.request.receiveStream
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import parsing.flat.Flat
import java.util.*


fun Application.module() {
    val logger = LoggerFactory.getLogger(SearchFlatServlet::class.java.name)

    // This adds Date and Server headers to each response, and allows custom additional headers
    install(DefaultHeaders)

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        allowCredentials = true
        host("api.vk.com")
    }

    routing {
        get("/") {
            call.respondText("It's server for apartments searching!", contentType = ContentType.Text.Plain)
        }

        post("/vk") {
            val utf8Str = call.receiveStream().bufferedReader(Charsets.UTF_8).use { it.readText() }

            val (text, status) = VkClient.handleRequest(utf8Str)
            call.respondText(text, status = status, contentType = ContentType.Text.Plain)
        }

        get("/parse_server") {
            runSearchFlatTask(139035212)
        }

        get("/parse") {
            fun sendFlat(flat: Flat) {
                VkMsgApi.sendFlat(139035212, flat)
            }

            transaction {
                val flatParameters = User.get(139035212).searchParameters.flatParameters

                val startTime = Date().time

                runBlocking {
                    logger.info("[${Thread.currentThread().name}] runBlocking")
                    val avitoUrl = UrlGenerator.getAvitoUrl(flatParameters)
                    println(avitoUrl)
                    val cianUrl = UrlGenerator.getCianUrl(flatParameters)

                    launch {
                        logger.info("[${Thread.currentThread().name}] launch parse avito")
                        AvitoParser.parseAsync(avitoUrl, ::sendFlat)
                    }
                    launch {
                        logger.info("[${Thread.currentThread().name}] launch parse cian")
                        CianParser.parseAsync(cianUrl, ::sendFlat)
                    }
                }

                logger.info("Complete by ${Date().time - startTime} ms")
            }
        }
    }
}
