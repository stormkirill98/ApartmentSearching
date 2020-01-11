package com.group.services.vk

import com.group.services.getProperty
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.messages.Keyboard
import com.vk.api.sdk.queries.messages.MessagesSendQuery
import org.slf4j.LoggerFactory
import kotlin.random.Random

object VkApi {
    private val accessKey = getProperty("vk-access-key")
    private val groupId = getProperty("vk-group-id").toInt()

    private val logger = LoggerFactory.getLogger(VkApi::class.java)

    private val vkApi = VkApiClient(HttpTransportClient.getInstance())
    private val actor = GroupActor(groupId, accessKey)

    fun sendMsg(peerId: Int, msg: String) {
        sendMsg(peerId, msg, null)
    }

    private fun sendMsg(peerId: Int, msg: String, keyboard: Keyboard? = null) {
        logger.info("Send msg to $peerId with text='$msg'")

        val sender = vkApi.messages()
            .send(actor)
            .randomId(Random.nextInt())
            .peerId(peerId)
            .message(msg)

        keyboard?.let { sender.keyboard(keyboard) }

        sender.execute()
    }
}