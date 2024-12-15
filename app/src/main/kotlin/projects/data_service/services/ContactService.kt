package po.playground.projects.data_service.services

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object Contacts : LongIdTable("contacts", "id") {
    val nowDateTime = LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    val name = varchar("name",128)
    val surname = varchar("legal_name",128)
}

class ContactEntity  (id: EntityID<Long>) : LongEntity(id){
    companion object : LongEntityClass<ContactEntity>(Contacts)
    var name by Contacts.name
    var surname by Contacts.surname
}