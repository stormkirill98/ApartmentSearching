package com.group.database

import com.group.services.vk.enums.LogicState
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.select
import org.postgresql.util.PGobject

object UserTable : IntIdTable() {
    val origin = customEnumeration("origin", "UserOrigin",
        { value -> UserOrigin.valueOf(value as String) },
        { PGEnum("\"UserOrigin\"", it) })

    val state = customEnumeration("state", "LogicState",
        { value -> LogicState.valueOf(value as String) },
        { PGEnum("\"LogicState\"", it) })

    val searchParametersId = reference("search_parameters_id", SearchParametersTable)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : EntityClass<Int, User>(UserTable) {
        fun exists(id: Int) = UserTable.select { UserTable.id eq id }.count() > 0
    }

    var origin by UserTable.origin
    var state by UserTable.state
    var searchParametersId by SearchParameters referencedOn UserTable.searchParametersId

    override fun toString(): String {
        return "User( ${id.value} $origin $state"
    }
}

enum class UserOrigin {
    NONE,
    VK,
    TELEGRAM
}

class PGEnum<T : Enum<T>>(enumTypeName: String, enumValue: T) : PGobject() {
    init {
        value = enumValue.name
        type = enumTypeName
    }
}