package com.group.services.vk

import com.group.services.getProperty
import com.vk.api.sdk.callback.CallbackApi
import com.vk.api.sdk.objects.callback.GroupJoin
import com.vk.api.sdk.objects.callback.GroupLeave
import com.vk.api.sdk.objects.messages.Message
import io.ktor.http.HttpStatusCode
import org.slf4j.LoggerFactory


object VkClient : CallbackApi() {
    private val CONFIRMED_KEY = getProperty("vk-confirmed-key")

    private val logger = LoggerFactory.getLogger(VkClient::class.java)

    fun handleRequest(body: String): Pair<String, HttpStatusCode> {
        logger.info(body)

        if (body.contains(""""type": "confirmation""""))
            return Pair(CONFIRMED_KEY, HttpStatusCode.OK)

        return if (parse(body))
            Pair("ok", HttpStatusCode.OK)
        else
            Pair("error", HttpStatusCode.BadRequest)
    }

    override fun messageNew(groupId: Int?, message: Message?) {
        message?.fromId?.let {
            VkApi.sendMsg(it, "Улыбочка)))")
        }
    }

    override fun messageEdit(groupId: Int?, message: Message?) {
        println("message edit")
        println(message.toString())

    }

    override fun groupJoin(groupId: Int?, message: GroupJoin?) {
        println("group join")
        println(message.toString())

    }

    override fun groupLeave(groupId: Int?, message: GroupLeave?) {
        println("group leave")
        println(message.toString())

    }
}