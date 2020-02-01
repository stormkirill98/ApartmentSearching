package com.group.services.vk

import com.group.database.User
import com.group.services.getProperty
import com.group.services.vk.enums.Command
import com.group.services.vk.enums.CountRoomCommand
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
                            flatParameters.addDistrict(districtId)
                            VkApi.selectedDistrictsMsg(msg.fromId, flatParameters.districts)
                        } else {
                            when(parseCommand(payload)) {
                                Command.NEXT -> {
                                    VkApi.roomsMsg(msg.fromId)
                                    user.state = LogicState.COUNT_ROOM
                                }

                                Command.CLEAR -> {
                                    flatParameters.clearDistricts()
                                    VkApi.selectedDistrictsMsg(msg.fromId, flatParameters.districts)
                                }

                                Command.ALL -> {
                                    flatParameters.setAllDistricts()

                                    VkApi.roomsMsg(msg.fromId)
                                    user.state = LogicState.COUNT_ROOM
                                }

                                else -> VkApi.wrongCommandMsg(msg.fromId)
                            }
                        }
                    }

                    LogicState.COUNT_ROOM -> {
                        when(parseCountRoomCommand(payload)) {
                            CountRoomCommand.ROOM_1 -> {
                                flatParameters.addCountRoom("1")
                                VkApi.selectedRoomsMsg(msg.fromId, flatParameters.rooms)
                            }

                            CountRoomCommand.ROOM_2 -> {
                                flatParameters.addCountRoom("2")
                                VkApi.selectedRoomsMsg(msg.fromId, flatParameters.rooms)
                            }

                            CountRoomCommand.ROOM_3 -> {
                                flatParameters.addCountRoom("3")
                                VkApi.selectedRoomsMsg(msg.fromId, flatParameters.rooms)
                            }

                            CountRoomCommand.ROOM_MORE_3 -> {
                                flatParameters.addCountRoom("3+")
                                VkApi.selectedRoomsMsg(msg.fromId, flatParameters.rooms)
                            }

                            CountRoomCommand.NEXT -> {
                                VkApi.priceMsg(msg.fromId)
                                user.state = LogicState.PRICE
                            }

                            CountRoomCommand.CLEAR -> {
                                flatParameters.clearRooms()
                                VkApi.selectedRoomsMsg(msg.fromId, flatParameters.rooms)
                            }

                            CountRoomCommand.ALL -> {
                                flatParameters.setAllRooms()

                                VkApi.priceMsg(msg.fromId)
                                user.state = LogicState.PRICE
                            }

                            else -> VkApi.wrongCommandMsg(msg.fromId)
                        }
                    }

                    LogicState.PRICE -> {
                        if (payload == null) {
                            val text = msg.text
                                .trim()
                                .replace(Regex(" +"), " ")
                                .toLowerCase()

                            if (!isPriceMsg(text)) {
                                VkApi.wrongPriceMsg(msg.fromId)
                                return@transaction
                            }

                            flatParameters.setPriceInterval(parsePrice(text))
                            user.state = LogicState.LANDLORD
                        } else {
                            when (parseCommand(payload)) {
                                Command.ALL -> {
                                    flatParameters.setAnyPrice()
                                    user.state = LogicState.LANDLORD
                                }
                                else -> VkApi.wrongCommandMsg(msg.fromId)
                            }
                        }
                    }

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

            transaction {
                val user = User.get(message.userId)
                user.state = LogicState.NOT_START
            }

            // TODO: stop searching
        }
    }
}