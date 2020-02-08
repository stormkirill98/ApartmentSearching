package com.group.services.vk

import com.group.database.User
import com.group.getProperty
import com.group.services.vk.enums.Command
import com.group.services.vk.enums.CountRoomCommand
import com.group.services.vk.enums.LandlordCommand
import com.group.services.vk.enums.LogicState
import com.group.tasks.removeSearchFlatTask
import com.group.tasks.runSearchFlatTask
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
                    VkMsgApi.groupJoinMsg(msg.fromId)
                    return@transaction
                }

                val user = User.get(msg.fromId)
                val flatParameters = user.searchParameters.flatParameters

                val payload = msg.payload
                when (user.state) {
                    LogicState.NOT_START -> {
                        if (payload == null) {
                            VkMsgApi.startMsg(msg.fromId)
                        } else {
                            val command = parseCommand(payload)

                            if (command == Command.START) {
                                VkMsgApi.districtsMsg(msg.fromId)
                                user.state = LogicState.DISTRICTS
                            } else VkMsgApi.startMsg(msg.fromId)
                        }
                    }

                    LogicState.CITY -> TODO()

                    LogicState.DISTRICTS -> {
                        if (payload == null) {
                            VkMsgApi.wrongCommandMsg(msg.fromId)
                            return@transaction
                        }

                        if (payload.contains("district")) {
                            val districtId = parseDistrict(payload)
                            flatParameters.addDistrict(districtId)
                        } else {
                            when (parseCommand(payload)) {
                                Command.NEXT -> {
                                    VkMsgApi.roomsMsg(msg.fromId)
                                    user.state = LogicState.COUNT_ROOM
                                }

                                Command.CLEAR -> {
                                    flatParameters.clearDistricts()
                                    VkMsgApi.notSelectDistrictsMsg(msg.fromId)
                                }

                                Command.ALL -> {
                                    flatParameters.setAllDistricts()

                                    VkMsgApi.roomsMsg(msg.fromId)
                                    user.state = LogicState.COUNT_ROOM
                                }

                                else -> VkMsgApi.wrongCommandMsg(msg.fromId)
                            }
                        }
                    }

                    LogicState.COUNT_ROOM -> {
                        when (parseCountRoomCommand(payload)) {
                            CountRoomCommand.ROOM_1 -> {
                                flatParameters.addCountRoom("1")
                            }

                            CountRoomCommand.ROOM_2 -> {
                                flatParameters.addCountRoom("2")
                            }

                            CountRoomCommand.ROOM_3 -> {
                                flatParameters.addCountRoom("3")
                            }

                            CountRoomCommand.ROOM_MORE_3 -> {
                                flatParameters.addCountRoom("3+")
                            }

                            CountRoomCommand.NEXT -> {
                                VkMsgApi.priceMsg(msg.fromId)
                                user.state = LogicState.PRICE
                            }

                            CountRoomCommand.CLEAR -> {
                                flatParameters.clearRooms()
                                VkMsgApi.notSelectRoomsMsg(msg.fromId)
                            }

                            CountRoomCommand.ALL -> {
                                flatParameters.setAllRooms()

                                VkMsgApi.priceMsg(msg.fromId)
                                user.state = LogicState.PRICE
                            }

                            else -> VkMsgApi.wrongCommandMsg(msg.fromId)
                        }
                    }

                    LogicState.PRICE -> {
                        if (payload == null) {
                            val text = msg.text
                                .trim()
                                .replace(Regex(" +"), " ")
                                .toLowerCase()

                            if (!isPriceMsg(text)) {
                                VkMsgApi.wrongPriceMsg(msg.fromId)
                                return@transaction
                            }

                            flatParameters.setPriceInterval(parsePrice(text))
                            user.state = LogicState.LANDLORD
                            VkMsgApi.landlordMsg(msg.fromId)
                        } else {
                            when (parseCommand(payload)) {
                                Command.ALL -> {
                                    flatParameters.setAnyPrice()
                                    user.state = LogicState.LANDLORD
                                    VkMsgApi.landlordMsg(msg.fromId)
                                }
                                else -> VkMsgApi.wrongCommandMsg(msg.fromId)
                            }
                        }
                    }

                    LogicState.LANDLORD -> {
                        if (payload == null) {
                            VkMsgApi.wrongCommandMsg(msg.fromId)
                        } else {
                            when (parseLandlordCommand(payload)) {
                                LandlordCommand.ALL -> {
                                    flatParameters.onlyOwner = false
                                    user.state = LogicState.CONFIRM
                                    VkMsgApi.confirmMsg(msg.fromId, flatParameters.getMsg())
                                }

                                LandlordCommand.ONLY_OWNER -> {
                                    flatParameters.onlyOwner = true
                                    user.state = LogicState.CONFIRM
                                    VkMsgApi.confirmMsg(msg.fromId, flatParameters.getMsg())
                                }

                                else -> VkMsgApi.wrongCommandMsg(msg.fromId)
                            }
                        }
                    }

                    LogicState.CONFIRM -> {
                        if (payload == null) {
                            VkMsgApi.wrongCommandMsg(msg.fromId)
                        } else {
                            when (parseCommand(payload)) {
                                Command.CHANGE -> {
                                    flatParameters.reset()
                                    user.state = LogicState.DISTRICTS
                                    VkMsgApi.districtsMsg(msg.fromId)
                                }

                                Command.START -> {
                                    user.state = LogicState.SEARCH_IN_PROGRESS
                                    VkMsgApi.searchMsg(msg.fromId)
                                    runSearch(user)
                                }

                                else -> VkMsgApi.wrongCommandMsg(msg.fromId)
                            }
                        }
                    }

                    LogicState.SEARCH_IN_PROGRESS -> {
                        if (payload == null) {
                            VkMsgApi.wrongCommandMsg(msg.fromId)
                        } else {
                            when (parseCommand(payload)) {
                                Command.CHANGE -> {
                                    flatParameters.reset()
                                    stopSearch(user)
                                    user.state = LogicState.DISTRICTS
                                    VkMsgApi.districtsMsg(msg.fromId)
                                }

                                Command.STOP -> {
                                    stopSearch(user)
                                    user.state = LogicState.WAIT
                                    VkMsgApi.waitMsg(msg.fromId)
                                }

                                else -> VkMsgApi.wrongCommandMsg(msg.fromId)
                            }
                        }
                    }

                    LogicState.WAIT -> {
                        if (payload == null) {
                            VkMsgApi.wrongCommandMsg(msg.fromId)
                        } else {
                            when (parseCommand(payload)) {
                                Command.CHANGE -> {
                                    flatParameters.reset()
                                    stopSearch(user)
                                    user.state = LogicState.DISTRICTS
                                    VkMsgApi.districtsMsg(msg.fromId)
                                }

                                Command.CONTINUE -> {
                                    user.state = LogicState.SEARCH_IN_PROGRESS
                                    VkMsgApi.searchMsg(msg.fromId)

                                    runSearch(user)
                                }

                                else -> VkMsgApi.wrongCommandMsg(msg.fromId)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun groupJoin(groupId: Int?, message: GroupJoin?) {
        message?.let {
            if (User.exists(it.userId)) {
                logger.info("Group Join User=${it.userId}. User is back")

                transaction {
                    val user = User.get(message.userId)
                    user.state = LogicState.WAIT
                }

                VkMsgApi.continueMsg(it.userId)
            } else {
                logger.info("Group Join User=${it.userId}. New user")

                User.newVkUser(it.userId)

                VkMsgApi.startMsg(it.userId)
            }
        }
    }

    override fun groupLeave(groupId: Int?, message: GroupLeave?) {
        message?.let {
            logger.info("Group Leave User='${it.userId}'")
            VkMsgApi.groupLeaveMsg(it.userId)

            transaction {
                val user = User.get(message.userId)
                user.state = LogicState.NOT_START
                stopSearch(user)
            }
        }
    }

    private fun runSearch(user: User) {
        if (user.searchParameters.flatParameters.taskId != null)
            stopSearch(user)

        user.searchParameters.flatParameters.taskId = runSearchFlatTask(user.id.value)
    }

    private fun stopSearch(user: User) {
        user.searchParameters.flatParameters.taskId?.let {
            removeSearchFlatTask(it)
            user.searchParameters.flatParameters.taskId = null
        }
    }
}