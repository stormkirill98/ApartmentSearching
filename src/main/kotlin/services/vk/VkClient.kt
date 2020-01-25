package com.group.services.vk

import com.group.datastore.entities.User
import com.group.services.getProperty
import com.group.services.vk.enums.Command
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

    override fun messageNew(groupId: Int?, msg: Message?) {
        msg?.let {
//            val user = UserDao.get(msg.fromId)

            /*if (user == null) {
                VkApi.sendMsg(msg.fromId, "Подпишитесь на группу, чтобы бот смог вам помочь")
                return
            }*/

//            val flatParameters = FlatParametersDao.get(user.flatParametersId)

            val payload = msg.payload
            /*when (user.currentState) {
                LogicState.NOT_START -> {
                   if (payload == null) {
                        VkApi.startMsg(msg.fromId)
                    } else {
                        val command = parseCommand(payload)

                        if (command == Command.START) {
                            VkApi.districtsMsg(msg.fromId)
                            user.currentState = LogicState.DISTRICT
                        }
                        else VkApi.startMsg(msg.fromId)
                    }
                }

                LogicState.CITY -> TODO()

                LogicState.DISTRICT -> {

                }

                LogicState.COUNT_ROOM -> TODO()
                LogicState.PRICE -> TODO()
                LogicState.LANDLORD -> TODO()
                LogicState.LEASE -> TODO()
                LogicState.SEARCH_IN_PROGRESS -> TODO()
            }*/

/*            UserDao.save(user)
            FlatParametersDao.save(flatParameters)*/
        }
    }

    override fun groupJoin(groupId: Int?, message: GroupJoin?) {
        message?.let {
            /*if (UserDao.exists(it.userId)) {
                logger.info("Group Join User=${it.userId}. User is back")
                VkApi.continueMsg(it.userId)
            } else {
                logger.info("Group Join User=${it.userId}. New user")

                val user = User.newVkUser(it.userId)
                UserDao.saveNow(user)

                VkApi.startMsg(it.userId)
            }*/
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