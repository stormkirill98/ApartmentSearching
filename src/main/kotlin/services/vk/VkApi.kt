package com.group.services.vk

import com.group.services.getProperty
import com.group.services.vk.enums.Keyboards
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import org.slf4j.LoggerFactory
import kotlin.random.Random

object VkApi {
    private val accessKey = getProperty("vk-access-key")
    private val groupId = getProperty("vk-group-id").toInt()

    private val logger = LoggerFactory.getLogger(VkApi::class.java)

    private val vkApi = VkApiClient(HttpTransportClient.getInstance())
    private val actor = GroupActor(groupId, accessKey)

    fun getUserName(id: Int): String {
        val result = vkApi.users()
            .get(actor)
            .userIds(id.toString())
            .execute()

        return result[0].firstName
    }

    fun startMsg(peerId: Int) {
        sendMsg(
            peerId,
            "Привет! " +
                    "Я помогу тебе снять квартиру. " +
                    "Для этого я буду присылать тебе новые объявления каждый час. " +
                    "Начнем-с?",
            Keyboards.START
        )
    }

    fun continueMsg(peerId: Int) {
        sendMsg(peerId, "Спасибо что вернулись. Давайте продолжим)", Keyboards.WAIT)
    }

    fun districtsMsg(peerId: Int) {
        sendMsg(peerId, "Выберите нужные районы", Keyboards.YAROSLAVL_DISTRICTS)
    }

    fun roomsMsg(peerId: Int) {
        sendMsg(peerId, "Выберите кол-во квартир, которое вам подходит", Keyboards.COUNT_ROOMS)
    }

    fun selectedDistrictsMsg(peerId: Int, districts: String) {
        //TODO: output id's, but need names
        val msg = if (districts.isEmpty())
            "Нет выбранных районов. Квартиры будут искаться по всему городу"
        else "Выбранные районы: $districts"
        sendMsg(peerId, msg)
    }

    fun selectedRoomsMsg(peerId: Int, rooms: String) {
        val msg = if (rooms.isEmpty())
            "Кол-во комнат не задано. Квартиры будут искаться с любым кол-вом комнат"
        else "Выбранное кол-во комнат: $rooms"
        sendMsg(peerId, msg)
    }

    fun priceMsg(peerId: Int) {
        sendMsg(
            peerId,
            "Введите диапазон цен в формате: от XXXX до XXXX(также возможны варианты: от XXXX, до XXXX)",
            Keyboards.PRICE
        )
    }

    fun landlordMsg(peerId: Int) {
        sendMsg(peerId, "Показывать квартиры только от собственника или от агенств тоже?", Keyboards.LANDLORDS)
    }

    fun confirmMsg(peerId: Int, text:String) {
        sendMsg(peerId, text, Keyboards.CONFIRM)
    }

    fun searchMsg(peerId: Int) {
        sendMsg(peerId, "Поиск начался, ожидайте подходящие для вас квартиры", Keyboards.MAIN)
    }

    fun waitMsg(peerId: Int) {
        sendMsg(peerId, "Поиск приостановлен. Если хотите продолжить выберите действие", Keyboards.WAIT)
    }

    fun groupLeaveMsg(peerId: Int) {
        sendMsg(peerId, "Вы уходите? Надемся вы нашли, что искали)", Keyboards.EMPTY)
    }

    fun wrongPriceMsg(peerId: Int) {
        sendMsg(
            peerId,
            "Введенный диапазон цен не соответствует формату\n" +
                  "Правильный формат: от XXXX до XXXX(также возможны варианты: от XXXX, до XXXX)",
            Keyboards.PRICE
        )
    }

    fun wrongCommandMsg(peerId: Int) {
        sendMsg(peerId, "Неверная команда")
    }

    fun sendMsg(peerId: Int, msg: String, keyboard: Keyboards? = null) {
        logger.info("Send msg to $peerId with text='$msg'")

        val sender = vkApi.messages()
            .send(actor)
            .randomId(Random.nextInt())
            .peerId(peerId)
            .message(msg)

        keyboard?.let { sender.keyboard(keyboard.keyboard) }

        sender.execute()
    }
}