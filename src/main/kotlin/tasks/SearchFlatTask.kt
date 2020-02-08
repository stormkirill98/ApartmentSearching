package com.group.tasks

import com.google.api.gax.rpc.ApiException
import com.google.cloud.tasks.v2.*
import com.google.protobuf.Timestamp
import com.group.UrlGenerator
import com.group.database.User
import com.group.parsing.flat.AvitoParser
import com.group.parsing.flat.CianParser
import com.group.services.vk.VkMsgApi
import com.group.services.vk.enums.LogicState
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import parsing.flat.Flat
import java.time.Clock
import java.time.Instant
import java.util.*
import javax.servlet.annotation.WebServlet
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private const val URL_PATTERN = "/tasks/search/flat"
private const val QUEUE_NAME = "search-flat"

@WebServlet(name = "Search Apartment", urlPatterns = [URL_PATTERN])
class SearchFlatServlet : HttpServlet() {
    private var userId: Int = 0
    private var oneFlatFound = false
    private val logger = LoggerFactory.getLogger(SearchFlatServlet::class.java.name)

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val userIdStr = req.getParameter("userId")
        if (userIdStr.isNullOrBlank()) {
            removeTask(req)
            return
        }

        userId = userIdStr.toInt()

        val prevCode = req.getHeader("X-AppEngine-TaskPreviousResponse")
        prevCode?.let {
            if (prevCode.toInt() != HttpServletResponse.SC_CONTINUE) {
                removeTask(req)
                resetUserToWaitState(userId)
                return
            }
        }

        transaction {
            val flatParameters = User.get(userId).searchParameters.flatParameters

            val startTime = Date().time

            runBlocking {
                logger.info("[${Thread.currentThread().name}] runBlocking")
                val avitoUrl = UrlGenerator.getAvitoUrl(flatParameters)
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

        if (!oneFlatFound) VkMsgApi.notFoundFlats(userId)
        resp.status = HttpServletResponse.SC_CONTINUE
    }

    private fun sendFlat(flat: Flat) {
        oneFlatFound = true
        VkMsgApi.sendFlat(userId, flat)
    }

    private fun removeTask(req: HttpServletRequest) {
        val taskName = req.getHeader("X-AppEngine-TaskName")
        removeSearchFlatTask("projects/$PROJECT_ID/locations/$LOCATION/queues/my-queue-id/tasks/$taskName")
    }

    private fun resetUserToWaitState(userId: Int) {
        transaction {
            val user = User.get(userId)
            user.state = LogicState.WAIT
            user.searchParameters.flatParameters.taskId = null
        }
        VkMsgApi.waitMsg(userId)
    }
}

fun runSearchFlatTask(userId: Int): String {
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

fun removeSearchFlatTask(taskId: String) {
    CloudTasksClient.create().use {
        try {
            it.deleteTask(taskId.trim())
        } catch (e: ApiException) {
        }
    }
}