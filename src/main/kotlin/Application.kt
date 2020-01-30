package com.group

import com.group.database.*
import com.group.services.vk.VkClient
import com.group.services.vk.enums.LogicState
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.request.receiveText
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.routing
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random


@Suppress("unused") // Referenced in application.conf
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

        get("/test") {
            val savedUser = transaction {
                addLogger(StdOutSqlLogger)

                val flatSearchParameters = FlatSearchParametersDao.new(Random.nextInt(-10000000, 0)) {
                    city = "Yaroslavl"
                    districts = Districts().toString()
                    rooms = Rooms().toString()
                    priceInterval = Price().toString()
                    onlyOwner = true
                }

                val searchParameters = SearchParameters.new(Random.nextInt(-10000000, 0)) {
                    flatParameters = flatSearchParameters
                }

                UserDao.new(Random.nextInt(-10000000, 0)) {
                    origin = UserOrigin.NONE
                    state = LogicState.NOT_START
                    searchParametersId = searchParameters
                }
            }

            call.respondText("For test was save user: $savedUser", contentType = ContentType.Text.Plain)
        }

        get("/get_flat") {
            val flatSearchParameters = FlatSearchParametersDao.getObject(86)

            call.respondText("Get flat parameters: $flatSearchParameters", contentType = ContentType.Text.Plain)
        }

        get("save_flat") {
            val flatSearchParameters = FlatSearchParameters(
                city = "Yaroslavl",
                districts = Districts(),
                rooms = Rooms(),
                priceInterval = Price(),
                onlyOwner = true
            )

            val savedFlatSearchParameters = FlatSearchParametersDao.saveObject(flatSearchParameters)

            call.respondText("Save flat parameters: $savedFlatSearchParameters", contentType = ContentType.Text.Plain)
        }

        post("/vk") {
            val (text, status) = VkClient.handleRequest(call.receiveText())
            call.respondText(text, status = status, contentType = ContentType.Text.Plain)
        }
    }
}
