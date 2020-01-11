package com.group

import com.googlecode.objectify.ObjectifyService
import com.group.datastore.entities.User
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener

@Suppress("unused")
class Bootstrapper : ServletContextListener {
    override fun contextInitialized(sce: ServletContextEvent?) {
        println("         contextInitialized")
        ObjectifyService.init()
        ObjectifyService.register(User::class.java)
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {}
}