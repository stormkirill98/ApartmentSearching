package com.group.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object SearchParametersTable : IntIdTable() {
    val flatParameters = reference("flat_parameters_id", FlatSearchParametersTable)
}

class SearchParameters(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SearchParameters>(SearchParametersTable)

    var flatParameters by FlatSearchParametersDao referencedOn SearchParametersTable.flatParameters
}