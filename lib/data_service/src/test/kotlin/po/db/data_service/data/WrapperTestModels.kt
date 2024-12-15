package po.db.data_service.data

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import po.db.data_service.dto.interfaces.DTOModel
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO


class TestDataModel : DataModel  {
    override var id :Long = 0
    val name: String = "SomeName"
}

class TestChildDataModel : DataModel  {
    override var id :Long = 0
    val name: String = "SomeDepartmentName"
}

object TestPartners : LongIdTable("partners", "id") {
    val nowDateTime = LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    val name = varchar("name",128)
    val legalName = varchar("legal_name",128)
    val regNr = varchar("reg_nr",45).nullable()
    val vatNr = varchar("vat_nr",45).nullable()
    val created = datetime("created").default(nowDateTime)
    val updated = datetime("updated").default(nowDateTime)
}

object TestDepartments : LongIdTable("departments", "id") {
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

class TestEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<TestEntity>(TestPartners)
    var name: String = "Test Partner"
}

class TestDTO(
    override val dataModel: TestDataModel,
) : CommonDTO<TestDataModel, TestEntity>(dataModel), DTOModel {
    override val className: String = "TestDTO"
}

class TestChildEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<TestChildEntity>(TestDepartments)
    var name: String = "TestChildEntity"
}

class TestChildDTO(
    override val dataModel: TestChildDataModel,
) : CommonDTO<TestChildDataModel, TestChildEntity>(dataModel), DTOModel {
    override val className: String = "TestChildDTO"
}

