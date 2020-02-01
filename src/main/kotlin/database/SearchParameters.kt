package com.group.database

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.transactions.transaction

object SearchParametersTable : IntIdTable() {
    val flatParameters = reference("flat_parameters_id", FlatSearchParametersTable)
}

class SearchParameters(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<SearchParameters>(SearchParametersTable) {
        fun newByDefault() = transaction {
            val flatSearchParameters = FlatSearchParameters.newByDefault()

            return@transaction new {
                flatParameters = flatSearchParameters
            }
        }
    }

    var flatParameters by FlatSearchParameters referencedOn SearchParametersTable.flatParameters
}