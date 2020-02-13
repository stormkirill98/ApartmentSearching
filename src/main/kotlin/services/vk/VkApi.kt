package com.group.services.vk

import com.group.getPhoto
import com.group.getProperty
import com.group.services.vk.enums.Keyboards
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.photos.Photo
import com.vk.api.sdk.queries.messages.MessagesSendQuery
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import parsing.flat.Flat
import java.text.SimpleDateFormat
import kotlin.random.Random

object VkMsgApi {
    private val logger = LoggerFactory.getLogger(VkApi::class.java)

    fun startMsg(id: Int) {
        VkApi.sendMsg(
            id,
            "Привет! " +
                    "Я помогу тебе снять квартиру. " +
                    "Для этого я буду присылать тебе новые объявления каждый час. " +
                    "Начнем?",
            Keyboards.START
        )
    }

    fun startKeyboard(id: Int) = VkApi.sendMsg(id, "", Keyboards.START)

    fun continueMsg(id: Int) {
        VkApi.sendMsg(id, "Спасибо что вернулись. Давайте продолжим)", Keyboards.WAIT)
    }

    fun continueKeyboard(id: Int) = VkApi.sendMsg(id, "", Keyboards.WAIT)

    fun districtsMsg(id: Int) {
        VkApi.sendMsg(id, "Выберите нужные районы", Keyboards.YAROSLAVL_DISTRICTS)
    }

    fun districtsKeyboard(id: Int) = VkApi.sendMsg(id, "", Keyboards.YAROSLAVL_DISTRICTS)

    fun notSelectDistrictsMsg(id: Int) {
        VkApi.sendMsg(id, "Нет выбранных районов. Квартиры будут искаться по всему городу")
    }

    fun roomsMsg(id: Int) {
        VkApi.sendMsg(id, "Выберите кол-во комнат, которое вам подходит", Keyboards.COUNT_ROOMS)
    }

    fun roomsKeyboard(id: Int) = VkApi.sendMsg(id, "", Keyboards.COUNT_ROOMS)

    fun notSelectRoomsMsg(id: Int) {
        VkApi.sendMsg(id, "Кол-во комнат не задано. Квартиры будут искаться с любым кол-вом комнат")
    }

    fun priceMsg(id: Int) {
        VkApi.sendMsg(
            id,
            "Введите диапазон цен в формате: от XXX до XXX(также возможны варианты: от XXX, до XXX)",
            Keyboards.PRICE
        )
    }

    fun priceKeyboard(id: Int) = VkApi.sendMsg(id, "", Keyboards.PRICE)

    fun landlordMsg(id: Int) {
        VkApi.sendMsg(id, "Показывать квартиры только от собственника или от агенств тоже?", Keyboards.LANDLORDS)
    }

    fun landlordKeyboard(id: Int) = VkApi.sendMsg(id, "", Keyboards.LANDLORDS)

    fun confirmMsg(id: Int, text: String) {
        VkApi.sendMsg(id, text, Keyboards.CONFIRM)
    }

    fun confirmKeyboard(id: Int) = VkApi.sendMsg(id, "", Keyboards.CONFIRM)

    fun searchMsg(id: Int) {
        VkApi.sendMsg(id, "Поиск начался, ожидайте подходящие для вас квартиры", Keyboards.MAIN)
    }

    fun searchKeyboard(id: Int) = VkApi.sendMsg(id, "", Keyboards.MAIN)

    fun waitMsg(id: Int) {
        VkApi.sendMsg(id, "Поиск приостановлен. Если хотите продолжить выберите действие", Keyboards.WAIT)
    }

    fun waitKeyboard(id: Int) = VkApi.sendMsg(id, "", Keyboards.WAIT)

    fun groupLeaveMsg(id: Int) {
        VkApi.sendMsg(id, "Вы уходите? Надемся вы нашли, что искали)", Keyboards.EMPTY)
    }

    fun wrongPriceMsg(id: Int) {
        VkApi.sendMsg(
            id,
            "Введенный диапазон цен не соответствует формату.\nПример: от 5000 до 10000"
        )
    }

    fun wrongCommandMsg(id: Int) {
        VkApi.sendMsg(id, "Неверная команда\n\\keyboard - получить текущую клавиатуру")
    }

    fun sendFlat(id: Int, flat: Flat) {
        val dateFormat = SimpleDateFormat("MM-dd HH:mm")

        logger.info("Send flat: ${flat.name} ${dateFormat.format(flat.date.time)} to $id")

        VkApi.createSender(id,
            """ 
                ${flat.name}
                Выложено ${dateFormat.format(flat.date.time)}
                Цена: ${flat.price}
                Адрес: ${flat.address}
                ${flat.url}
            """.trimIndent())
            .attachment(VkApi.getPhotoAttachments(flat.images))
            .execute()
    }

    fun notFoundFlats(id: Int) {
        VkApi.sendMsg(id, "За последний час не было выложено новых квартир")
    }

    fun groupJoinMsg(id: Int) {
        VkApi.sendMsg(id,"Подпишитесь на группу, чтобы бот смог вам помочь")
    }
}

object VkApi {
    private val accessKey = getProperty("vk-access-key")
    private val groupId = getProperty("vk-group-id").toInt()

    private val logger = LoggerFactory.getLogger(VkApi::class.java)

    private val vkApi = VkApiClient(HttpTransportClient.getInstance())
    private val actor = GroupActor(groupId, accessKey)

    fun sendMsg(id: Int, msg: String, keyboard: Keyboards? = null) {
        logger.info("Send msg to $id with text='$msg'")

        val sender = createSender(id, msg)

        keyboard?.let { sender.keyboard(keyboard.keyboard) }

        sender.execute()
    }

    fun createSender(id: Int, msg: String): MessagesSendQuery = vkApi.messages().send(actor).randomId(Random.nextInt()).peerId(id).message(msg)

    fun getPhotoAttachments(imageUrls: List<String>): String {
        val imageThreads = arrayListOf<Deferred<Photo>>()
        val attachments = StringBuilder()

        runBlocking {
            logger.info("[${Thread.currentThread().name}] runBlocking for getPhotoAttachments")
            for (imageUrl in imageUrls) {
                if (imageUrl.isBlank())
                    continue

                val photoThread = async {
                    logger.info("[${Thread.currentThread().name}] async savePhoto")
                    savePhoto(imageUrl)
                }
                imageThreads.add(photoThread)
            }

            imageThreads.forEach { attachments.append(attachmentFromPhoto(it.await())).append(",") }
        }

        return attachments.toString()
    }

    private fun attachmentFromPhoto(photo: Photo) = "photo${photo.ownerId}_${photo.id}"

    private fun savePhoto(url: String): Photo {
        val photo = getPhoto(url)

        val uploadServer = vkApi.photos()
            .getMessagesUploadServer(actor)
            .execute()

        val uploadResponse = vkApi.upload()
            .photoMessage(uploadServer.uploadUrl.toString(), photo)
            .execute()

        photo.delete()

        val photos = vkApi.photos()
            .saveMessagesPhoto(actor, uploadResponse.photo)
            .server(uploadResponse.server)
            .hash(uploadResponse.hash)
            .execute()

        return photos.last()
    }
}
