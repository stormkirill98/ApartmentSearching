package com.group.database

import com.group.UrlGenerator
import com.group.database.UserTable.nullable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

private const val CITY_NAME_LENGTH = 100
private const val TASK_ID_LENGTH = 500

object FlatSearchParametersTable : IntIdTable() {
    val city = varchar("city", CITY_NAME_LENGTH)
    val districts = text("districts")
    val rooms = text("rooms")
    val startPrice = integer("startPrice")
    val endPrice = integer("endPrice")
    val onlyOwner = bool("onlyOwner")
    val taskId = varchar("task_id", TASK_ID_LENGTH).nullable()
}

class FlatSearchParameters(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FlatSearchParameters>(FlatSearchParametersTable) {
        fun newByDefault() = transaction {
            return@transaction new {
                city = "ярославль"
                districts = ""
                rooms = ""
                startPrice = 0
                endPrice = 0
                onlyOwner = false
            }
        }
    }

    var city by FlatSearchParametersTable.city
    var districts by FlatSearchParametersTable.districts
    var rooms by FlatSearchParametersTable.rooms
    var startPrice by FlatSearchParametersTable.startPrice
    var endPrice by FlatSearchParametersTable.endPrice
    var onlyOwner by FlatSearchParametersTable.onlyOwner
    var taskId by FlatSearchParametersTable.taskId

    fun addDistrict(districtId: String) {
        if (!districts.contains(districtId)) {
            districts += "$districtId-"
        }
    }

    fun clearDistricts() {
        districts = ""
    }

    fun setAllDistricts() = clearDistricts()

    fun addCountRoom(countRoom: String) {
        if (!rooms.contains(countRoom)) {
            rooms += "$countRoom,"
        }
    }

    fun clearRooms() {
        rooms = ""
    }

    fun setAllRooms() = clearRooms()

    fun setPriceInterval(priceInterval: Pair<Int, Int>) {
        if (priceInterval.second > priceInterval.first) {
            startPrice = priceInterval.first
            endPrice = priceInterval.second
        } else {
            startPrice = priceInterval.second
            endPrice = priceInterval.first
        }
    }

    fun setAnyPrice() = setPriceInterval(0 to 0)

    fun getMsg(): String {
        val priceInterval = when {
            startPrice == 0 && endPrice == 0 -> "любая"
            startPrice == 0 -> "до $endPrice"
            endPrice == 0 -> "от $startPrice"
            else -> "от $startPrice до $endPrice"
        }

        val avitoUrl = UrlGenerator.getAvitoUrl(this)

        return """
            Проверьте параметры поиска, которые вы задали:
            
            1. $city
            
            2. Районы: ${ if (districts.isEmpty()) "любой" else districts.dropLast(1) }
            
            3. Кол-во комнат: ${ if (rooms.isEmpty()) "любое" else rooms.dropLast(1) }
            
            4. Цена: $priceInterval
            
            5. ${ if (onlyOwner) "Только собственник" else "От собственника и от агенства" }
            
            Сгенерированная ссылка на авито: $avitoUrl
        """.trimIndent()
    }
}