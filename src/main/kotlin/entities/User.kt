package com.group.entities

import com.googlecode.objectify.annotation.Entity
import com.googlecode.objectify.annotation.Id

@Entity
data class User(@Id val id: String, val origin: UserOrigin){
    constructor() : this("", UserOrigin.NONE)
}

enum class UserOrigin {
    NONE,
    VK
}