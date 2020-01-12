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