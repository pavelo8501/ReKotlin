package po.exposify.test.data

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import po.db.data_service.classes.DTOClass
import po.db.data_service.test.data.TestPartners.nowDateTime
import po.db.data_service.classes.interfaces.DTOModel
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.models.EntityDTO


class TestPartnerDataModel(
    val name: String = "SomeName",
    val legalName : String = "SomeLegalName",
) : DataModel  {
    override var id :Long = 0L

    val departnemts = mutableListOf<TestDepartmentDataModel>()

    val created = nowDateTime
    val updated = nowDateTime
}

class TestDepartmentDataModel(
    val hq: Boolean = true,
    val name: String = "SomeDepartmentName",
    var frequency: Int = 12,
) : DataModel{
    override var id :Long = 0
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

class TestPartnerEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<TestPartnerEntity>(TestPartners)
    var name: String = "Test Partner"
}

class TestPartnerDTO(
    override val dataModel: TestPartnerDataModel
): EntityDTO<TestPartnerDataModel, TestPartnerEntity>(dataModel), DTOModel{

    companion object : DTOClass<TestPartnerDataModel, TestPartnerEntity>(TestPartnerDTO::class){
        override fun setup() {
            TODO("Not yet implemented")
        }
    }
}


class TestDepartmentEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<TestDepartmentEntity>(TestDepartments)
    var name: String = "TestChildEntity"
}

class TestDepartmentDTO(
    override val dataModel: TestDepartmentDataModel
) : EntityDTO<TestDepartmentDataModel, TestDepartmentEntity>(dataModel), DTOModel{

    companion object : DTOClass<TestDepartmentDataModel, TestDepartmentEntity>(TestDepartmentDTO::class){
        override fun setup() {
            TODO("Not yet implemented")
        }
    }

}


