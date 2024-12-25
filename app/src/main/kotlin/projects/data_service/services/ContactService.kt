package po.playground.projects.data_service.services

import org.jetbrains.exposed.dao.id.LongIdTable

object Contacts : LongIdTable("contacts", "id") {

    val name = varchar("name",128)
    val surname = varchar("surname",128)
}

