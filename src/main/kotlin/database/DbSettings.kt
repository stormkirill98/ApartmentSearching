package com.group.database

import org.jetbrains.exposed.sql.Database

object DbSettings {
    val db by lazy {
        Database.connect(
            "jdbc:postgresql://35.242.227.75:5432/postgres",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "admin"
        )
    }
}