package po.exposify.test

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime


fun tables(): List<LongIdTable>{
    return  listOf<LongIdTable>(TestPartners, TestDepartments, TestInspections,TestContacts)
}


object TestPartners : LongIdTable("test_partners", "id") {
    val nowDateTime = LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    val name = varchar("name",128)
    val legalName = varchar("legal_name",128)
    val regNr = varchar("reg_nr",45).nullable()
    val vatNr = varchar("vat_nr",45).nullable()
    val created = datetime("created").default(nowDateTime)
    val updated = datetime("updated").default(nowDateTime)
}

object TestDepartments : LongIdTable("test_departments", "id") {
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
    val partner = reference("partner", TestPartners)
}

object TestInspections : LongIdTable("test_inspections", "id") {
    val nowDateTime = LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    val time = datetime("time")
    val created = datetime("created").default(nowDateTime)
    val updated = datetime("updated").default(nowDateTime)
    val department = reference("department", TestDepartments)
}

object TestContacts: LongIdTable("test_contacts", "id"){
    val nowDateTime = LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    val dds = bool("DDS")
    val name= varchar("name",64)
    val surname= varchar("surname",64).nullable()
    val position = varchar("position",64).nullable()
    val phone = varchar("phone",16).nullable()
    val email = varchar("email",64).nullable()
    val created = datetime("created").default(TestDepartments.nowDateTime)
    val updated = datetime("updated").default(TestDepartments.nowDateTime)
    val partner = reference("partner", TestPartners).uniqueIndex()
}