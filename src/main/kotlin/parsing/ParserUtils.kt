package com.group.parsing

import com.group.nowCalendar
import com.group.nowDate
import java.util.*

const val HOUR = 3_600_000

fun getDate(dateStr: String): Calendar {
    fun changeTime(dateStr: String, date: Calendar) {
        val nums = dateStr.split(":")

        date.set(Calendar.HOUR_OF_DAY, nums[0].toInt())
        date.set(Calendar.MINUTE, nums[1].toInt())
    }

    val date = nowCalendar()
    val handledDateStr = dateStr.trim().toLowerCase()

    when {
        handledDateStr.contains("сегодня") -> {
            changeTime(handledDateStr.substringAfter(" "), date)
        }

        handledDateStr.contains("вчера") -> {
            date.add(Calendar.DAY_OF_MONTH, -1)
            changeTime(handledDateStr.substringAfter(" "), date)
        }

        else -> date.clear()
    }

    return date
}

fun getDifferenceFromNow(date: Calendar): Int {
    return (nowDate().time - date.timeInMillis).toInt()
}