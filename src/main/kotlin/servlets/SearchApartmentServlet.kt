package com.group.servlets

import com.google.api.gax.rpc.ApiException
import com.google.cloud.tasks.v2.*
import com.google.protobuf.Timestamp
import com.group.UrlGenerator
import com.group.database.User
import com.group.parsing.Apartment
import com.group.parsing.AvitoParser
import com.group.services.vk.VkApi
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Clock
import java.time.Instant
import java.util.logging.Logger
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private const val URL_PATTERN = "/tasks/search/flat"
private const val QUEUE_NAME = "search-flat"

@WebServlet(name = "Search Apartment", urlPatterns = [URL_PATTERN])
class SearchApartmentServlet : HttpServlet() {
    private val log: Logger = Logger.getLogger(SearchApartmentServlet::class.java.name)
    private var userId: Int = 0;
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val userIdStr = req.getParameter("userId")
        if (userIdStr.isNullOrBlank()) {
            log.warning("Wrong user id")
            resp.status = HttpServletResponse.SC_BAD_REQUEST
            return
        }

        userId = userIdStr.toInt()

        transaction {
            val user = User.get(userId)

            val url = UrlGenerator.getAvitoUrl(user.searchParameters.flatParameters)
            AvitoParser.parse(url, ::sendFlat)
        }

        resp.status = HttpServletResponse.SC_BAD_REQUEST
    }

    private fun sendFlat(apartment: Apartment) {
        VkApi.sendApartment(userId, apartment)
    }
}

fun runSearchApartmentTask(userId: Int): String {
    CloudTasksClient.create().use {
        val queuePath = QueueName.of(PROJECT_ID, LOCATION, QUEUE_NAME).toString()

        val taskBuilder = Task.newBuilder()
            .setAppEngineHttpRequest(
                AppEngineHttpRequest
                    .newBuilder()
                    .setRelativeUri("$URL_PATTERN?userId=$userId")
                    .setHttpMethod(HttpMethod.GET)
                    .build()
            )

        taskBuilder.setScheduleTime(
            Timestamp
                .newBuilder()
                .setSeconds(Instant.now(Clock.systemUTC()).plusSeconds(5L).epochSecond)
        )

        val task = it.createTask(queuePath, taskBuilder.build())
        return task.name
    }
}

fun removeSearchApartmentTask(taskId: String) {
    CloudTasksClient.create().use {
        try {
            it.deleteTask(taskId.trim())
        } catch (e: ApiException) {}
    }
}