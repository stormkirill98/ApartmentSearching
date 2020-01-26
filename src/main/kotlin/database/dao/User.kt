package com.group.database.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

/*
object User: Table() {
    val id = varchar("id", 255)
    var origin = customEnumeration("origin", "origin", {value -> Foo.valueOf(value as String)}, { UserOrigin("FooEnum", it)})
}

class UserOrigin<T:Enum<T>>(enumTypeName: String, enumValue: T) : PGobject() {
    init {
        value = enumValue.name
        type = enumTypeName
    }
}*/

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