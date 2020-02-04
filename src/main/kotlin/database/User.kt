package com.group.database

import com.group.services.vk.enums.LogicState
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.postgresql.util.PGobject

private const val TASK_ID_LENGTH = 500

object UserTable : IntIdTable() {
    val origin = customEnumeration("origin", "UserOrigin",
        { value -> UserOrigin.valueOf(value as String) },
        { PGEnum("\"UserOrigin\"", it) })

    val state = customEnumeration("state", "LogicState",
        { value -> LogicState.valueOf(value as String) },
        { PGEnum("\"LogicState\"", it) })

    val taskId = varchar("task_id", TASK_ID_LENGTH).nullable()
    val searchParameters = reference("search_parameters_id", SearchParametersTable)
}

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : EntityClass<Int, User>(UserTable) {
        fun exists(id: Int) = transaction { UserTable.select { UserTable.id eq id }.count() > 0 }
        fun newVkUser(id: Int) = newUser(id, UserOrigin.VK)

        private fun newUser(id: Int, origin: UserOrigin) = transaction {
            val searchParameters = SearchParameters.newByDefault()

            return@transaction new(id) {
                this.origin = origin
                this.state = LogicState.NOT_START
                this.searchParameters = searchParameters
            }
        }
    }

    var origin by UserTable.origin
    var state by UserTable.state
    var taskId by UserTable.taskId
    var searchParameters by SearchParameters referencedOn UserTable.searchParameters

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