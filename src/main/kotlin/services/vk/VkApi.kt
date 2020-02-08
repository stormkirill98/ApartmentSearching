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

    fun startMsg(peerId: Int) {
        VkApi.sendMsg(
            peerId,
            "Привет! " +
                    "Я помогу тебе снять квартиру. " +
                    "Для этого я буду присылать тебе новые объявления каждый час. " +
                    "Начнем-с?",
            Keyboards.START
        )
    }

    fun continueMsg(peerId: Int) {
        VkApi.sendMsg(peerId, "Спасибо что вернулись. Давайте продолжим)", Keyboards.WAIT)
    }

    fun districtsMsg(peerId: Int) {
        VkApi.sendMsg(peerId, "Выберите нужные районы", Keyboards.YAROSLAVL_DISTRICTS)
    }

    fun roomsMsg(peerId: Int) {
        VkApi.sendMsg(peerId, "Выберите кол-во комнат, которое вам подходит", Keyboards.COUNT_ROOMS)
    }

    fun notSelectDistrictsMsg(peerId: Int) {
        VkApi.sendMsg(peerId, "Нет выбранных районов. Квартиры будут искаться по всему городу")
    }

    fun notSelectRoomsMsg(peerId: Int) {
        VkApi.sendMsg(peerId, "Кол-во комнат не задано. Квартиры будут искаться с любым кол-вом комнат")
    }

    fun priceMsg(peerId: Int) {
        VkApi.sendMsg(
            peerId,
            "Введите диапазон цен в формате: от XXX до XXX(также возможны варианты: от XXX, до XXX)",
            Keyboards.PRICE
        )
    }

    fun landlordMsg(peerId: Int) {
        VkApi.sendMsg(peerId, "Показывать квартиры только от собственника или от агенств тоже?", Keyboards.LANDLORDS)
    }

    fun confirmMsg(peerId: Int, text: String) {
        VkApi.sendMsg(peerId, text, Keyboards.CONFIRM)
    }

    fun searchMsg(peerId: Int) {
        VkApi.sendMsg(peerId, "Поиск начался, ожидайте подходящие для вас квартиры", Keyboards.MAIN)
    }

    fun waitMsg(peerId: Int) {
        VkApi.sendMsg(peerId, "Поиск приостановлен. Если хотите продолжить выберите действие", Keyboards.WAIT)
    }

    fun groupLeaveMsg(peerId: Int) {
        VkApi.sendMsg(peerId, "Вы уходите? Надемся вы нашли, что искали)", Keyboards.EMPTY)
    }

    fun wrongPriceMsg(peerId: Int) {
        VkApi.sendMsg(
            peerId,
            "Введенный диапазон цен не соответствует формату.\nПример: от 5000 до 10000"
        )
    }

    fun wrongCommandMsg(peerId: Int) {
        VkApi.sendMsg(peerId, "Неверная команда")
    }

    fun sendFlat(peerId: Int, flat: Flat) {
        val dateFormat = SimpleDateFormat("MM-dd HH:mm")

        logger.info("Send flat: ${flat.name} ${dateFormat.format(flat.date.time)} to $peerId")

        VkApi.createSender()
            .peerId(peerId)
            .message(
                """ 
                ${flat.name}
                Выложено ${dateFormat.format(flat.date.time)}
                Цена: ${flat.price}
                Адрес: ${flat.address}
                ${flat.url}
            """.trimIndent()
            )
            .attachment(VkApi.getPhotoAttachments(flat.images))
            .execute()
    }

    fun notFoundFlats(peerId: Int) {
        VkApi.sendMsg(peerId, "За последний час не было выложено новых квартир")
    }

    fun groupJoinMsg(peerId: Int) {
        VkApi.sendMsg(peerId,"Подпишитесь на группу, чтобы бот смог вам помочь")
    }
}

object VkApi {
    private val accessKey = getProperty("vk-access-key")
    private val groupId = getProperty("vk-group-id").toInt()

    private val logger = LoggerFactory.getLogger(VkApi::class.java)

    private val vkApi = VkApiClient(HttpTransportClient.getInstance())
    private val actor = GroupActor(groupId, accessKey)

    fun sendMsg(peerId: Int, msg: String, keyboard: Keyboards? = null) {
        logger.info("Send msg to $peerId with text='$msg'")

        val sender = createSender()
            .peerId(peerId)
            .message(msg)

        keyboard?.let { sender.keyboard(keyboard.keyboard) }

        sender.execute()
    }

    fun createSender(): MessagesSendQuery = vkApi.messages().send(actor).randomId(Random.nextInt())

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
