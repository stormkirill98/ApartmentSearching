package com.group.services.vk

import com.group.datastore.dao.FlatParametersDao
import com.group.datastore.dao.UserDao
import com.group.datastore.entities.Districts
import com.group.datastore.entities.FlatParameters
import com.group.datastore.entities.User
import com.group.services.getProperty
import com.group.services.vk.enums.Commands
import com.group.services.vk.enums.LogicState
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
            val user = UserDao.get(message.fromId)

            if (user == null) {
                // TODO: add button for join to group
                // TODO: check that user is not group member, if member then create he
                VkApi.sendMsg(message.fromId, "Подпишитесь на группу, чтобы бот смог вам помочь")
                return
            }

            val flatParameters = FlatParametersDao.get(user.parameters.flatParametersId)

            if (it.payload == null) {
                if (user.currentState == LogicState.CITY) {
                    // TODO: check that right city name
                    flatParameters.city = transliterateCyrillicToLatin(message.text)
                    user.currentState = LogicState.DISTRICT

                    // TODO: get districts by city
                    VkApi.districtsMsg(it.fromId, Districts())
                }

                return
            }

            when (parseCommand(it.payload)) {
                Commands.START -> {
                    user.currentState = LogicState.START

                    VkApi.cityMsg(it.fromId)
                }

                else -> {
                    logger.warn("Message='${it.text}' is not parsed")
                    VkApi.sendMsg(it.fromId, "Не понял, что вы имеете ввиду, ${VkApi.getUserName(message.fromId)}")
                }
            }

            UserDao.save(user)
            FlatParametersDao.save(flatParameters)
        }
    }

    override fun groupJoin(groupId: Int?, message: GroupJoin?) {
        message?.let {
            if (UserDao.exists(it.userId)) {
                logger.info("Group Join User=${it.userId}. User is back")
                VkApi.continueMsg(it.userId)
            } else {
                logger.info("Group Join User=${it.userId}. New user")

                val user = User.newVkUser(it.userId)
                UserDao.saveNow(user)

                VkApi.startMsg(it.userId)
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