package com.thewhite.models

import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime


/**
 * Created on 29.12.2022.
 *<p>
 *
 * @author Denis Matytsin
 */
object SystemSettings : IdTable<String>("system_setting") {

    val key = varchar("key", 255).uniqueIndex()
    val value = text("value").nullable()
    val createdDate = datetime("created_date").defaultExpression(CurrentDateTime)
    val updatedDate = datetime("updated_date").defaultExpression(CurrentDateTime)

    override val id = key.entityId()
    override val primaryKey: PrimaryKey = PrimaryKey(key)
}

class SystemSetting(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, SystemSetting>(SystemSettings)

    var key by SystemSettings.key
    var value by SystemSettings.value
    val createdDate by SystemSettings.createdDate
    var updatedDate by SystemSettings.updatedDate
}