package po.playground.projects.data_service.services

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.kotlin.datetime.datetime


object Contacts: LongIdTable("contacts", "id"){
    val nowDateTime = LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    val dds = bool("DDS")
    val name= varchar("name",64)
    val surname= varchar("surname",64).nullable()
    val position = varchar("position",64).nullable()
    val phone = varchar("phone",16).nullable()
    val email = varchar("email",64).nullable()
    val created = datetime("created").default(Departments.nowDateTime)
    val updated = datetime("updated").default(Departments.nowDateTime)
    val partner = reference("partner", Partners).uniqueIndex()
}
