package com.group.services.vk

import com.group.services.getProperty
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import com.vk.api.sdk.objects.messages.*
import org.slf4j.LoggerFactory
import kotlin.random.Random

object VkApi {
    private val accessKey = getProperty("vk-access-key")
    private val groupId = getProperty("vk-group-id").toInt()

    private val logger = LoggerFactory.getLogger(VkClient::class.java)

    // TODO not work on server, need enable billing
    private val vkApi = VkApiClient(HttpTransportClient.getInstance())
    private val actor = GroupActor(groupId, accessKey)

    fun sendMsg(peerId: Int, msg: String) {
        val keyboard = listOf(
            listOf(
                KeyboardButton().apply {
                    color = KeyboardButtonColor.NEGATIVE
                    action = KeyboardButtonAction().apply {
                        type = KeyboardButtonActionType.TEXT
                        payload = "{\"command\":\"start\"}"
                        label = "123dsa"
                    }

                }
            )
        )

        vkApi.messages()
            .send(actor)
            .peerId(peerId)
            .randomId(Random.nextInt())
            .message(msg)
            .keyboard(
                Keyboard()
                    .setInline(false)
                    .setOneTime(true)
                    .setButtons(
                        keyboard
                    )
            )
            .execute()
    }
}