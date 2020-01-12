package com.group.datastore.entities

import com.googlecode.objectify.annotation.Entity
import com.googlecode.objectify.annotation.Id
import com.group.services.vk.enums.LogicState

@Entity
class User private constructor(
    @Id val id: String,
    var origin: UserOrigin,
    var currentState: LogicState
) {
    constructor() : this("", UserOrigin.NONE, LogicState.NOT_START)

    override fun toString(): String {
        return "User(id=$id origin=$origin currentState=$currentState)"
    }

    companion object {
        fun newVkUser(id: Int) = User(id.toString(), UserOrigin.VK, LogicState.NOT_START)
    }
}

enum class UserOrigin {
    NONE,
    VK
}