package com.group

import org.jetbrains.exposed.sql.Database
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

@Suppress("unused")
class Bootstrapper : ServletContextListener {
    override fun contextInitialized(sce: ServletContextEvent?) {
        Database.connect(
            "jdbc:postgresql://35.242.227.75:5432/postgres",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "admin"
        )
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {}
}