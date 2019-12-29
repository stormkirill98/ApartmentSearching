package com.group.services.vk

import org.slf4j.LoggerFactory
import java.util.*

object VkClient {
    private val accessKey: String
    private val logger = LoggerFactory.getLogger(VkClient::class.java)

    init {
        val propFile = VkClient::class.java.getResource("/config.properties")
        val props = Properties()

        props.load(propFile.openStream())

        accessKey = props.getProperty("vk-access-key")
    }


    fun handleRequest(body: String) {
        logger.debug(accessKey)
        logger.debug(body)
    }
}