package com.group

import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import java.net.InetAddress
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

@Suppress("unused")
class Bootstrapper : ServletContextListener {
    private val logger = LoggerFactory.getLogger(Bootstrapper::class.java)

    override fun contextInitialized(sce: ServletContextEvent) {
        // maybe is KOSTYL. Add your host to white list!
        if (InetAddress.getLoopbackAddress().hostAddress == "127.0.0.1") {
            Database.connect(
                "jdbc:postgresql://35.242.227.75:5432/postgres",
                driver = "org.postgresql.Driver",
                user = "postgres",
                password = "admin"
            )
        } else {
            Database.connect(
                "jdbc:postgresql:///postgres" +
                        "?cloudSqlInstance=apartment-searching:europe-west3:myinstance" +
                        "&socketFactory=com.google.cloud.sql.postgres.SocketFactory",
                driver = "org.postgresql.Driver",
                user = "postgres",
                password = "admin"
            )
        }
    }

    override fun contextDestroyed(sce: ServletContextEvent) {}
}