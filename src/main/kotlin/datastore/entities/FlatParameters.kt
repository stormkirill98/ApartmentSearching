package com.group.datastore.entities

import com.googlecode.objectify.annotation.Entity
import com.googlecode.objectify.annotation.Id

@Entity
class FlatParameters(
    val city: String = "",
    val districts: Districts = Districts(),
    val rooms: Rooms = Rooms(),
    val price: Price = Price.any(),
    val owner: Boolean = false,
    val daily: Boolean = false
) {
    @Id val id: Long? = null

    override fun toString(): String {
        return "Parameters (id='$id' city='$city' districts='$districts' $rooms price='$price' " +
                "${if (owner) "owner" else "all_landlords"} ${if (daily) "daily" else "long_time"}"
    }
}

data class Districts(val list: List<String> = arrayListOf())

data class Price(override val start: Int, override val endInclusive: Int) : ClosedRange<Int> {
    fun isAny() = start == Int.MIN_VALUE && endInclusive == Int.MAX_VALUE

    override fun toString(): String {
        return if (isAny())
            "Price (Any)"
        else super.toString()
    }

    companion object {
        fun any(): Price = Price(Int.MIN_VALUE, Int.MAX_VALUE)
    }
}

data class Rooms(
    val one: Boolean = false,
    val two: Boolean = false,
    val three: Boolean = false,
    val four: Boolean = false,
    val five: Boolean = false,
    val six: Boolean = false,
    val studio: Boolean = false
) {
    fun isAll() = !one && !two && !three && !four && !five && !six && !studio

    override fun toString(): String {
        var result = "Rooms ("

        if (isAll())
            return "Rooms (All)"

        when {
            one -> result += "1,"
            two -> result += "2,"
            three -> result += "3,"
            four -> result += "4,"
            five -> result += "5,"
            six -> result += "6,"
            studio -> result += "STUDIO,"
        }

        result = result.dropLast(1) + ")"

        return result
    }
}
