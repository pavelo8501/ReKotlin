package po.playground.projects.data_service.services

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import po.playground.projects.data_service.dto.DepartmentEntity
import po.playground.projects.data_service.dto.PartnerEntity


object Inspections : LongIdTable("inspections", "id") {
    val nowDateTime = LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    val time = datetime("time")
    val created = datetime("created").default(nowDateTime)
    val updated = datetime("updated").default(nowDateTime)
    val department = reference("department", Departments)
}