package com.group.datastore.dao

import com.group.datastore.entities.User

object UserDao : BaseDao<User>(User::class.java)