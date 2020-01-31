package com.group.services.vk

import com.group.database.Districts
import com.group.database.User
import com.group.services.getProperty
import com.group.services.vk.enums.Command
import com.group.services.vk.enums.LogicState
import com.vk.api.sdk.callback.CallbackApi
import com.vk.api.sdk.objects.callback.GroupJoin
import com.vk.api.sdk.objects.callback.GroupLeave
import com.vk.api.sdk.objects.messages.Message
import io.ktor.http.HttpStatusCode
import org.jetbrains.exposed.sql.transactions.transaction
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

    override fun messageNew(groupId: Int?, msg: Message?) {
        msg?.let {
            transaction {
                if (!User.exists(msg.fromId)) {
                    VkApi.sendMsg(msg.fromId, "Подпишитесь на группу, чтобы бот смог вам помочь")
                    return@transaction
                }

                val user = User.get(msg.fromId)
                val flatParameters = user.searchParameters.flatParameters

                val payload = msg.payload
                when (user.state) {
                    LogicState.NOT_START -> {
                        if (payload == null) {
                            VkApi.startMsg(msg.fromId)
                        } else {
                            val command = parseCommand(payload)

                            if (command == Command.START) {
                                VkApi.districtsMsg(msg.fromId)
                                user.state = LogicState.DISTRICTS
                            } else VkApi.startMsg(msg.fromId)
                        }
                    }

                    LogicState.CITY -> TODO()

                    LogicState.DISTRICTS -> {
                        if (payload.contains("district")) {
                            val districtId = parseDistrict(payload)
                            // TODO: send msg, that district was saved
                            flatParameters.addDistrict(districtId)
                        } else {
                            val command = parseCommand(payload)

                            if (command == Command.NEXT) {
                                VkApi.roomsMsg(msg.fromId)
                            } else {
                                TODO("Handle wrong command(it is error)")
                            }
                        }
                    }

                    LogicState.COUNT_ROOM -> TODO()
                    LogicState.PRICE -> TODO()
                    LogicState.LANDLORD -> TODO()
                    LogicState.SEARCH_IN_PROGRESS -> TODO()
                }
            }
        }
    }

    override fun groupJoin(groupId: Int?, message: GroupJoin?) {
        message?.let {
            if (User.exists(it.userId)) {
                logger.info("Group Join User=${it.userId}. User is back")
                VkApi.continueMsg(it.userId)
            } else {
                logger.info("Group Join User=${it.userId}. New user")

                User.newVkUser(it.userId)

                VkApi.startMsg(it.userId)
            }
        }
    }

    override fun groupLeave(groupId: Int?, message: GroupLeave?) {
        message?.let {
            logger.info("Group Leave User='${it.userId}'")
            VkApi.sendMsg(it.userId, "Вы уходите? Надемся вы нашли, что искали)")
            // TODO: set state NOT_START, stop searching
        }
    }
}