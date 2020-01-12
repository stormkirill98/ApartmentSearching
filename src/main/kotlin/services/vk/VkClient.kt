package com.group.services.vk

import com.group.datastore.dao.UserDao
import com.group.datastore.entities.User
import com.group.services.getProperty
import com.group.services.vk.enums.Keyboards
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
        message?.let {
            when(parseCommand(it.payload)) {

                else -> {
                    logger.warn("Message='${it.text}' is not parsed")
                    VkApi.sendMsg(it.fromId, "Не понял, что вы имеете ввиду")
                }
            }
        }
    }

    override fun groupJoin(groupId: Int?, message: GroupJoin?) {
        message?.let {
            if (UserDao.exists(it.userId)) {
                logger.info("Group Join User=${it.userId}. User is back")
                VkApi.sendMsg(it.userId, "Спасибо что вернулись. Давайте продолжим)", Keyboards.Continue)
            } else {
                logger.info("Group Join User=${it.userId}. New user")

                val user = User.newVkUser(it.userId)
                UserDao.saveNow(user)

                VkApi.sendMsg(it.userId,"Привет! Начнем-с?", Keyboards.START)
            }
        }
    }

    override fun groupLeave(groupId: Int?, message: GroupLeave?) {
        message?.let {
            logger.info("Group Leave User='${it.userId}'")
            VkApi.sendMsg(it.userId, "Вы уходите? Надемся вы нашли, что искали)")
        }
    }
}