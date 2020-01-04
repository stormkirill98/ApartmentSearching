package com.group.entities

import com.googlecode.objectify.annotation.Id

data class Parameters(val searchSubject: String) {
    @Id val id: Long = 0


}