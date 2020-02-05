package com.group

import com.google.appengine.api.utils.SystemProperty
import com.group.services.vk.VkClient
import java.io.File
import java.net.URL
import java.util.*
import javax.imageio.ImageIO

fun getProperty(propName: String): String {
    val propFile = VkClient::class.java.getResource("/config.properties")
    val props = Properties()

    props.load(propFile.openStream())

    return props.getProperty(propName)
}

fun nowDate(): Date = nowCalendar().time

fun nowCalendar(): Calendar {
    val date = Calendar.getInstance()
    if (isProduction())
        date.add(Calendar.HOUR_OF_DAY, 3)
    return date
}

fun isProduction() = SystemProperty.environment.value() == SystemProperty.Environment.Value.Production

fun getPhoto(url: String): File {
    val img = ImageIO.read(URL(url))
    val file = File("/tmp/${url.substringAfterLast("/")}")
    ImageIO.write(img, "jpg", file)
    return file
}