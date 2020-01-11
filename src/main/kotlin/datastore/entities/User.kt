package com.group.datastore.entities

import com.googlecode.objectify.annotation.Entity
import com.googlecode.objectify.annotation.Id

@Entity
data class User(@Id val id: String?, var origin: UserOrigin){
    constructor() : this(null, UserOrigin.NONE)
}

enum class UserOrigin {
    NONE,
    VK
}