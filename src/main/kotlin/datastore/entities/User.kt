package com.group.datastore.entities

import com.googlecode.objectify.annotation.Entity
import com.googlecode.objectify.annotation.Id
import com.group.datastore.dao.FlatParametersDao
import com.group.services.vk.enums.LogicState

@Entity
class User private constructor(
    @Id val id: String,
    var origin: UserOrigin,
    var currentState: LogicState = LogicState.NOT_START,
    var parameters: Parameters = Parameters()
) {

    @Suppress("unused") // need for objectify
    constructor() : this("", UserOrigin.NONE)

    override fun toString(): String {
        return "User(id=$id origin=$origin currentState=$currentState) $parameters"
    }

    companion object {
        fun newVkUser(id: Int) = User(id.toString(), UserOrigin.VK)
    }
}

class Parameters {
    val flatParametersId: Long = FlatParametersDao.saveAndReturn(FlatParameters()).id ?: 0L
}

enum class UserOrigin {
    NONE,
    VK
}
