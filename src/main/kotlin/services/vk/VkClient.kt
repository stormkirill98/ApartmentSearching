package com.group.services.vk

import com.group.database.FlatSearchParameters
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

                if (msg.text != null && msg.text == "\\keyboard") {
                    resendCurrentKeyboard(user)
                    return@transaction
                }

                when (user.state) {
                    LogicState.NOT_START -> handleNotStart(user, msg)
                    LogicState.CITY -> TODO()
                    LogicState.DISTRICTS -> handleDistricts(user, flatParameters, msg)
                    LogicState.COUNT_ROOM -> handleCountRoom(user, flatParameters, msg)
                    LogicState.PRICE -> handlePrice(user, flatParameters, msg)
                    LogicState.LANDLORD -> handleLandlord(user, flatParameters, msg)
                    LogicState.CONFIRM -> handleConfirm(user, flatParameters, msg)
                    LogicState.SEARCH_IN_PROGRESS -> handleSearchInProgress(user, flatParameters, msg)
                    LogicState.WAIT -> handleWait(user, flatParameters, msg)
                }
            }
        }
    }

    private fun resendCurrentKeyboard(user: User) {
        when(user.state) {
            LogicState.NOT_START -> VkMsgApi.startKeyboard(user.id.value)
            LogicState.CITY -> TODO()
            LogicState.DISTRICTS -> VkMsgApi.districtsKeyboard(user.id.value)
            LogicState.COUNT_ROOM -> VkMsgApi.roomsKeyboard(user.id.value)
            LogicState.PRICE -> VkMsgApi.priceKeyboard(user.id.value)
            LogicState.LANDLORD -> VkMsgApi.landlordKeyboard(user.id.value)
            LogicState.CONFIRM -> VkMsgApi.confirmKeyboard(user.id.value)
            LogicState.SEARCH_IN_PROGRESS -> VkMsgApi.searchKeyboard(user.id.value)
            LogicState.WAIT -> VkMsgApi.waitKeyboard(user.id.value)
        }
    }

    override fun groupJoin(groupId: Int?, message: GroupJoin?) {
        // TODO: wait /start msg for start dialog
        // TODO: check that user can use keyboards
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

    private fun handleNotStart(user: User, msg: Message) {
        if (msg.payload == null) {
            VkMsgApi.startMsg(msg.fromId)
            return
        }

        if (parseCommand(msg.payload) == Command.START) {
            VkMsgApi.districtsMsg(msg.fromId)
            user.state = LogicState.DISTRICTS
        } else VkMsgApi.startMsg(msg.fromId)
    }

    private fun handleDistricts(user: User, flatParameters: FlatSearchParameters, msg: Message) {
        if (msg.payload == null) {
            VkMsgApi.wrongCommandMsg(msg.fromId)
            return
        }

        if (msg.payload.contains("district")) {
            val districtId = parseDistrict(msg.payload)
            flatParameters.addDistrict(districtId)
        } else {
            when (parseCommand(msg.payload)) {
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

    private fun handleCountRoom(
        user: User,
        flatParameters: FlatSearchParameters,
        msg: Message
    ) {
        when (parseCountRoomCommand(msg.payload)) {
            CountRoomCommand.ROOM_1 -> flatParameters.addCountRoom("1")
            CountRoomCommand.ROOM_2 -> flatParameters.addCountRoom("2")
            CountRoomCommand.ROOM_3 -> flatParameters.addCountRoom("3")
            CountRoomCommand.ROOM_MORE_3 -> flatParameters.addCountRoom("3+")

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

    private fun handlePrice(
        user: User,
        flatParameters: FlatSearchParameters,
        msg: Message
    ) {
        if (msg.payload == null) {
            val text = msg.text
                .trim()
                .replace(Regex(" +"), " ")
                .toLowerCase()

            if (!isPriceMsg(text)) {
                VkMsgApi.wrongPriceMsg(msg.fromId)
                return
            }

            flatParameters.setPriceInterval(parsePrice(text))
            user.state = LogicState.LANDLORD
            VkMsgApi.landlordMsg(msg.fromId)
        } else {
            when (parseCommand(msg.payload)) {
                Command.ALL -> {
                    flatParameters.setAnyPrice()
                    user.state = LogicState.LANDLORD
                    VkMsgApi.landlordMsg(msg.fromId)
                }
                else -> VkMsgApi.wrongCommandMsg(msg.fromId)
            }
        }
    }

    private fun handleLandlord(
        user: User,
        flatParameters: FlatSearchParameters,
        msg: Message
    ) {
        if (msg.payload == null) {
            VkMsgApi.wrongCommandMsg(msg.fromId)
            return
        }

        when (parseLandlordCommand(msg.payload)) {
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


    private fun handleConfirm(
        user: User,
        flatParameters: FlatSearchParameters,
        msg: Message
    ) {
        if (msg.payload == null) {
            VkMsgApi.wrongCommandMsg(msg.fromId)
        }

        when (parseCommand(msg.payload)) {
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

    private fun handleSearchInProgress(
        user: User,
        flatParameters: FlatSearchParameters,
        msg: Message
    ) {
        if (msg.payload == null) {
            VkMsgApi.wrongCommandMsg(msg.fromId)
        }

        when (parseCommand(msg.payload)) {
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

    private fun handleWait(
        user: User,
        flatParameters: FlatSearchParameters,
        msg: Message
    ) {
        if (msg.payload == null) {
            VkMsgApi.wrongCommandMsg(msg.fromId)
            return
        }

        when (parseCommand(msg.payload)) {
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
