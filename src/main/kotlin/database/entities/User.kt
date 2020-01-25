package com.group.datastore.entities

import com.group.services.vk.enums.LogicState

class User private constructor(
    val id: String,
    var origin: UserOrigin,
    var currentState: LogicState = LogicState.NOT_START,
    var flatParametersId: Long = 0L
) {

    @Suppress("unused") // need for objectify
    constructor() : this("", UserOrigin.NONE)

    override fun toString(): String {
        return "User(id=$id origin=$origin currentState=$currentState flatParametersId=$flatParametersId)"
    }

    companion object {
        fun newVkUser(id: Int): User {
            val user = User(id.toString(), UserOrigin.VK)

/*            val flatParameters = FlatParametersDao.saveAndReturn(FlatParameters())
            user.flatParametersId = flatParameters.id ?: 0L*/

            return user
        }
    }
}

enum class UserOrigin {
    NONE,
    VK
}
