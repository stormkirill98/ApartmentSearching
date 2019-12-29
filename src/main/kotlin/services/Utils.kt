package com.group.services

import com.group.services.vk.VkClient
import java.util.*

fun getProperty(propName: String): String {
    val propFile = VkClient::class.java.getResource("/config.properties")
    val props = Properties()

    props.load(propFile.openStream())

    return props.getProperty(propName)
}