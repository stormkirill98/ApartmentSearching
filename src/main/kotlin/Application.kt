package com.group

import com.group.database.User
import com.group.parsing.AvitoParser
import com.group.parsing.CianParser
import com.group.parsing.Flat
import com.group.services.vk.VkApi
import com.group.services.vk.VkClient
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.request.receiveStream
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import org.jetbrains.exposed.sql.transactions.transaction


fun Application.module() {
    // This adds Date and Server headers to each response, and allows custom additional headers
    install(DefaultHeaders)
    // This uses use the logger to log every call (request/response)
    install(CallLogging)

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

        get("/parse") {
            fun sendFlat(flat: Flat) {
                VkApi.sendFlat(139035212, flat)
            }
            transaction {
                val user = User.get(139035212)

//                val avitoUrl = UrlGenerator.getAvitoUrl(user.searchParameters.flatParameters)
//                AvitoParser.parse(avitoUrl, ::sendFlat)

                val cianUrl = UrlGenerator.getCianUrl(user.searchParameters.flatParameters)
                println(cianUrl)

                CianParser.parse(cianUrl, ::sendFlat)
            }
        }
    }
}
