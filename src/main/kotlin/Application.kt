package com.group

import com.group.database.*
import com.group.database.entities.Districts
import com.group.database.entities.Price
import com.group.database.entities.Rooms
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
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction


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
            transaction {
                addLogger(StdOutSqlLogger)

                val flatSearchParameters = FlatSearchParameters.new {
                    city = "Yaroslavl"
                    districts = Districts().toString()
                    rooms = Rooms().toString()
                    priceInterval = Price().toString()
                    onlyOwner = true
                }

                val searchParameters = SearchParameters.new {
                    flatParameters = flatSearchParameters
                }

                User.new(12312) {
                    origin = UserOrigin.NONE
                    state = LogicState.NOT_START
                    this.searchParameters = searchParameters
                }
            }

            call.respondText("It's server for apartments searching!", contentType = ContentType.Text.Plain)
        }

        get("/test") {
            //            val user = UserDao.saveAndReturn(User.newVkUser(123123123))
//            call.respondText(user.toString(), contentType = ContentType.Text.Plain)

            /*val users = UserDao.listAll()
            call.respondText(
                FlatParameters().apply {
                    rooms.apply { one = true; two = true; studio = true };
                    price = Price(1000, 5000);
                    districts.add("District 1"); districts.add("District 2")
                }.toString(),
                contentType = ContentType.Text.Plain
            )*/
        }

        post("/vk") {
            val (text, status) = VkClient.handleRequest(call.receiveText())
            call.respondText(text, status = status, contentType = ContentType.Text.Plain)
        }
    }
}
