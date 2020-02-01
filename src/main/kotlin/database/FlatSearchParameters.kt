package com.group.database

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
    val startPrice = integer("startPrice")
    val endPrice = integer("endPrice")
    val onlyOwner = bool("onlyOwner")
}

class FlatSearchParameters(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FlatSearchParameters>(FlatSearchParametersTable) {
        fun newByDefault() = transaction {
            return@transaction new {
                city = ""
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