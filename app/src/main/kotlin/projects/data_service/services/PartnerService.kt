package po.playground.projects.data_service.services

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Partners : LongIdTable("partners", "id") {
    val nowDateTime = LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    val name = varchar("name",128)
    val legalName = varchar("legal_name",128)
    val regNr = varchar("reg_nr",45).nullable()
    val vatNr = varchar("vat_nr",45).nullable()
    val created = datetime("created").default(nowDateTime)
    val updated = datetime("updated").default(nowDateTime)
}