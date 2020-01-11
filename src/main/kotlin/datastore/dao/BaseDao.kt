package com.group.datastore.dao

import com.googlecode.objectify.Key
import com.googlecode.objectify.ObjectifyService.ofy
import com.googlecode.objectify.Ref
import com.googlecode.objectify.cmd.LoadType
import com.googlecode.objectify.cmd.Query
import org.slf4j.LoggerFactory

abstract class BaseDao<T>
protected constructor(private val clazz: Class<T>) {
    private val logger = LoggerFactory.getLogger(BaseDao::class.java)

    fun listAll(): List<T> {
        return ofy().load().type(clazz).list()
    }

    fun save(entity: T) {
        ofy().save().entity(entity)
    }

    fun save(entities: Iterable<T>) {
        ofy().save().entities(entities)
    }

    fun saveNow(entities: Iterable<T>) {
        ofy().save().entities(entities).now()
    }

    fun saveNow(entity: T): Key<T> {
        return ofy().save().entity(entity).now()
    }

    fun saveAndReturn(entity: T): T {
        return get(saveNow(entity))
    }

    fun saveAndReturnKey(entity: T): Key<T> {
        return ofy().save().entity(entity).now()
    }

    operator fun get(id: Long): T {
        return ofy().load().type(clazz).id(id).now()
    }

    operator fun get(stringId: String): T {
        return get(java.lang.Long.valueOf(stringId))
    }

    operator fun get(key: Key<T>): T {
        return ofy().load().key(key).now()
    }

    operator fun get(parent: Ref<T>, id: Long): T {
        return ofy().load().type(clazz).parent(parent).id(id).now()
    }

    operator fun get(parent: Ref<T>, id: String): T {
        return get(parent, java.lang.Long.valueOf(id))
    }

    operator fun get(parent: T, id: Long): T {
        return ofy().load().type(clazz).parent(parent).id(id).now()
    }

    fun getByProperty(propName: String, propValue: Any): T? {
        var q: Query<T> = ofy().load().type(clazz)
        q = q.filter(propName, propValue)

        val fetch: Iterator<T> = q.limit(2).list().iterator()
        if (!fetch.hasNext()) {
            return null
        }

        val obj = fetch.next()
        if (fetch.hasNext()) {
            logger.error("Too Many Results $clazz get by property $propName with value $propValue")
        }

        return obj
    }

    fun exists(id: Long): Boolean {
        return get(id) != null
    }

    fun exists(id: String): Boolean {
        return get(id) != null
    }

    fun delete(obj: T) {
        ofy().delete().entity(obj)
    }

    fun delete(id: Long) {
        ofy().delete().type(clazz).id(id)
    }

    protected fun query(): LoadType<T> {
        return ofy().load().type(clazz)
    }

}