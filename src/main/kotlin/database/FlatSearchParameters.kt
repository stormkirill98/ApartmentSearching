package com.group.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

const val CITY_NAME_LENGTH = 100

object FlatSearchParametersTable : IntIdTable() {
    val city = varchar("city", CITY_NAME_LENGTH)
    val districts = text("districts")
    val rooms = text("rooms")
    val priceInterval = text("priceInterval")
    val onlyOwner = bool("onlyOwner")
}

class FlatSearchParameters(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FlatSearchParameters>(FlatSearchParametersTable)

    var city by FlatSearchParametersTable.city
    var districts by FlatSearchParametersTable.districts
    var rooms by FlatSearchParametersTable.rooms
    var priceInterval by FlatSearchParametersTable.priceInterval
    var onlyOwner by FlatSearchParametersTable.onlyOwner

    override fun toString(): String {
        return "FlatParameters(id='$id' city='$city' $districts $rooms $priceInterval " +
                (if (onlyOwner) "only_owner" else "all_landlords") + ")"
    }
}