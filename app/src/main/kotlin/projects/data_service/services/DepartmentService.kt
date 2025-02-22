package po.playground.projects.data_service.services

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Departments : LongIdTable("departments", "id") {
    val nowDateTime = LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    val hq = bool("hq")
    val name = varchar("name",64)
    val street = varchar("street",128).nullable()
    val city = varchar("city",64).nullable()
    val country = varchar("country",64).nullable()
    val postCode = varchar("post_code",8).nullable()
    val phone = varchar("phone",45).nullable()
    val email = varchar("email",45).nullable()
    val frequency = integer("frequency").default(12)
    val lastInspection = datetime("last_inspection").nullable()
    val created = datetime("created").default(nowDateTime)
    val updated = datetime("updated").default(nowDateTime)
    val partner = reference("partner", Partners)
}
