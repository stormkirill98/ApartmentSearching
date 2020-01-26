package com.group.database

import com.group.services.vk.enums.LogicState
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.postgresql.util.PGobject

object UserTable : IdTable<String>() {
    val origin = customEnumeration("origin", "UserOrigin",
        { value -> UserOrigin.valueOf(value as String) },
        { PGEnum("\"UserOrigin\"", it) })
    val state = customEnumeration("state", "LogicState",
        { value -> LogicState.valueOf(value as String) },
        { PGEnum("\"LogicState\"", it) })

    val parameters = reference("search_parameters_id", SearchParametersTable)

    override val id = varchar("id", 255).entityId()
    override val primaryKey = PrimaryKey(id)

}

class User(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, User>(UserTable)

    var origin by UserTable.origin
    var state by UserTable.state
    var searchParameters by SearchParameters referencedOn UserTable.parameters
}

enum class UserOrigin {
    NONE,
    VK,
    TELEGRAM
}

class PGEnum<T:Enum<T>>(enumTypeName: String, enumValue: T?) : PGobject() {
    init {
        value = enumValue?.name
        type = enumTypeName
    }
}