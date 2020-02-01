package com.group.database

import com.google.gson.Gson
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

const val CITY_NAME_LENGTH = 100

object FlatSearchParametersTable : IntIdTable() {
    val city = varchar("city", CITY_NAME_LENGTH)
    val districts = text("districts")
    val rooms = text("rooms")
    val priceInterval = text("priceInterval")
    val onlyOwner = bool("onlyOwner")
}

class FlatSearchParameters(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FlatSearchParameters>(FlatSearchParametersTable) {
        fun newByDefault() = transaction {
            return@transaction new {
                city = ""
                districts = ""
                rooms = Rooms()
                priceInterval = Price()
                onlyOwner = false
            }
        }
    }

    // TODO: transform выполняется на каждое обращение к полю
    var city by FlatSearchParametersTable.city
    var districts by FlatSearchParametersTable.districts
    var rooms by FlatSearchParametersTable.rooms.transform(
        { Gson().toJson(it) },
        { Gson().fromJson(it, Rooms::class.java) }
    )
    var priceInterval by FlatSearchParametersTable.priceInterval.transform(
        { Gson().toJson(it) },
        { Gson().fromJson(it, Price::class.java) }
    )

    var onlyOwner by FlatSearchParametersTable.onlyOwner

    fun addDistrict(districtId: String) =
        if (!districts.contains(districtId)) {
            districts += "$districtId,"
            true
        } else false

    fun clearDistricts() {
        districts = ""
    }

    fun addDistricts(districts: ArrayList<Int>) {
        this.districts = districts.joinToString()
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