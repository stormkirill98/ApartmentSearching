package com.group

import org.jetbrains.exposed.sql.Database
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

@Suppress("unused")
class Bootstrapper : ServletContextListener {
    override fun contextInitialized(sce: ServletContextEvent) {
        // maybe is KOSTYL. Add your host to white list!
        if (isProduction()) {
            Database.connect(
                "jdbc:postgresql:///postgres" +
                        "?cloudSqlInstance=apartment-searching:europe-west3:myinstance" +
                        "&socketFactory=com.google.cloud.sql.postgres.SocketFactory",
                driver = "org.postgresql.Driver",
                user = "postgres",
                password = "admin"
            )
        } else {
            Database.connect(
                "jdbc:postgresql://35.242.227.75:5432/postgres",
                driver = "org.postgresql.Driver",
                user = "postgres",
                password = "admin"
            )
        }

    }

    override fun contextDestroyed(sce: ServletContextEvent) {}
}