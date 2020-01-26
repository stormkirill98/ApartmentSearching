package com.group.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object FlatSearchParameters : IntIdTable() {
    val city = varchar("city", 100)
    val districts = text("districts")
    val rooms = text("rooms")
    val priceInterval = text("priceInterval")
    val onlyOwner = bool("onlyOwner")
}

class FlatSearchParameter(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<FlatSearchParameter>(FlatSearchParameters)

    var city by FlatSearchParameters.city
    var districts by FlatSearchParameters.districts
    var rooms by FlatSearchParameters.rooms
    var priceInterval by FlatSearchParameters.priceInterval
    var onlyOwner by FlatSearchParameters.onlyOwner

    override fun toString(): String {
        return "FlatParameters(id='$id' city='$city' $districts $rooms $priceInterval " +
                (if (onlyOwner) "only_owner" else "all_landlords") + ")"
    }
}