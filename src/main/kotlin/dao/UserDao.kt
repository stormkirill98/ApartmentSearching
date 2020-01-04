package com.group.dao

import com.group.entities.User

object UserDao : BaseDao<User>(User::class.java)