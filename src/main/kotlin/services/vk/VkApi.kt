package com.group.services.vk

import com.group.services.getProperty
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient
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
        vkApi.messages()
            .send(actor)
            .peerId(peerId)
            .randomId(Random.nextInt())
            .message(msg)
            .execute()
    }
}