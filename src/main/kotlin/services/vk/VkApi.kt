package com.group.services.vk

import com.group.services.getProperty
import org.slf4j.LoggerFactory

object VkApi {
    private val accessKey = getProperty("vk-access-key")
    private val logger = LoggerFactory.getLogger(VkClient::class.java)
}