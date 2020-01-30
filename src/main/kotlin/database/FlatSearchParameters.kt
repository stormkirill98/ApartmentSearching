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

class FlatSearchParametersDao(id: EntityID<Int>) : IntEntity(id) {
    var city by FlatSearchParametersTable.city
    var districts by FlatSearchParametersTable.districts
    var rooms by FlatSearchParametersTable.rooms
    var priceInterval by FlatSearchParametersTable.priceInterval
    var onlyOwner by FlatSearchParametersTable.onlyOwner

    companion object : IntEntityClass<FlatSearchParametersDao>(FlatSearchParametersTable) {
        fun getObject(id: Int): FlatSearchParameters {
            val dao = transaction { get(id) }
            return FlatSearchParameters(
                dao.id.value,
                dao.city.trim(),
                Gson().fromJson(dao.districts, Districts::class.java),
                Gson().fromJson(dao.rooms, Rooms::class.java),
                Gson().fromJson(dao.priceInterval, Price::class.java),
                dao.onlyOwner
            )
        }

        fun saveObject(obj: FlatSearchParameters): FlatSearchParameters {
            return transaction {
                val flatSearchParametersDao = new {
                    city = obj.city
                    districts = Gson().toJson(obj.districts)
                    rooms = Gson().toJson(obj.rooms)
                    priceInterval = Gson().toJson(obj.priceInterval)
                    onlyOwner = obj.onlyOwner
                }

                obj.id = flatSearchParametersDao.id.value
                return@transaction obj
            }
        }
    }
}

data class FlatSearchParameters(
    var id: Int = 0,
    var city: String,
    val districts: Districts,
    val rooms: Rooms,
    val priceInterval: Price,
    var onlyOwner: Boolean
) {
    override fun toString(): String {
        return "FlatParameters(id='$id' city='$city' $districts $rooms $priceInterval " +
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