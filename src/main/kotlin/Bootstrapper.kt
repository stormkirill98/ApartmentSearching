package com.group

import org.jetbrains.exposed.sql.Database
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

@Suppress("unused")
class Bootstrapper : ServletContextListener {
    override fun contextInitialized(sce: ServletContextEvent?) {
        Database.connect(
            "jdbc:postgresql:///postgres" +
                    "?cloudSqlInstance=apartment-searching:europe-west3:myinstance" +
                    "&socketFactory=com.google.cloud.sql.postgres.SocketFactory" +
                    "&user=postgres&password=admin",
            driver = "org.postgresql.Driver"
        )
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {}
}