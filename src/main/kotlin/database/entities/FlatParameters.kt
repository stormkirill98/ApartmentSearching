package com.group.datastore.entities


class FlatParameters(
    var city: String = "",
    val districts: Districts = Districts(),
    val rooms: Rooms = Rooms(),
    var price: Price = Price.any(),
    var onlyOwner: Boolean = false
) {
    val id: Long? = null

    override fun toString(): String {
        return "FlatParameters(id='$id' city='$city' $districts $rooms $price " +
                (if (onlyOwner) "only_owner" else "all_landlords") + ")"
    }
}

class Districts : ArrayList<String>() {
    fun isAll() = isEmpty()

    override fun toString(): String {
        return if (isAll())
            "Districts(Any)"
        else "Districts${joinToString(prefix = "(", postfix = ")")}"
    }
}

data class Price(
    override var start: Int = Int.MIN_VALUE,
    override var endInclusive: Int = Int.MAX_VALUE
) : ClosedRange<Int> {
    fun isAny() = start == Int.MIN_VALUE && endInclusive == Int.MAX_VALUE

    override fun toString(): String {
        return if (isAny())
            "Price(Any)"
        else "Price[$start, $endInclusive]"
    }

    companion object {
        fun any(): Price = Price(Int.MIN_VALUE, Int.MAX_VALUE)
    }
}

data class Rooms(
    var one: Boolean = false,
    var two: Boolean = false,
    var three: Boolean = false,
    var four: Boolean = false,
    var five: Boolean = false,
    var six: Boolean = false,
    var studio: Boolean = false
) {
    fun isAll() = !one && !two && !three && !four && !five && !six && !studio

    override fun toString(): String {
        var result = "Rooms("

        if (isAll())
            return "Rooms(All)"

        if (one) result += "1,"
        if (two) result += "2,"
        if (three) result += "3,"
        if (four) result += "4,"
        if (five) result += "5,"
        if (six) result += "6,"
        if (studio) result += "Studio,"

        result = result.dropLast(1) + ")"

        return result
    }
}
