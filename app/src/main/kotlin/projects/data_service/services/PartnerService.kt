package po.playground.projects.data_service.services

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import po.db.data_service.DatabaseManager
import po.db.data_service.services.BasicDataService
import po.db.data_service.services.CoreService
import po.db.data_service.services.models.CoreDbEntity
import po.db.data_service.services.models.ServiceDBEntity
import po.playground.projects.data_service.DBManager
import po.playground.projects.data_service.services.models.DepartmentEntity
import po.playground.projects.data_service.services.models.PartnerModel

object Partners : LongIdTable("partners", "id") {
    public val nowDateTime = LocalDateTime.Companion.parse(Clock.System.now().toLocalDateTime(TimeZone.UTC).toString())
    val name=varchar("name",128)
    val legalName=varchar("legal_name",128)
    val regNr=varchar("reg_nr",45).nullable()
    val vatNr=varchar("vat_nr",45).nullable()
    val created = datetime("created").nullable().default(nowDateTime)
    val updated = datetime("updated").nullable().default(nowDateTime)
}


class PartnerEntity(id: EntityID<Long>) : CoreDbEntity(id){
    companion object : LongEntityClass<PartnerEntity>(Partners)

    var name by Partners.name
    var legalName by Partners.legalName
    var regNr by Partners.regNr
    var vatNr by Partners.vatNr
    var created by Partners.created
    var updated by Partners.updated
    val departments by  DepartmentEntity referrersOn Departments.partner

    fun toDataModel():PartnerModel{
        val result = PartnerModel(
            id.value,
            this.name,
            this.legalName,
            this.regNr,
            this.vatNr
        ).also {

            // it.departments = departments.map { it.toDataModel() }
        }
        return result
    }
}


object PartnerService : CoreService<PartnerModel>(PartnerModel::class,"partner"){

    override lateinit var  dbManager: DatabaseManager

    fun initialize(dbManager: DatabaseManager){
        this.dbManager = dbManager


    }


}


//class PartnerService(override val dbManager: DBManager) : BasicDataService<PartnerModel, PartnerEntity>(PartnerModel){
//
//    override val autoload: Boolean = true
//    override val table: LongIdTable = Partners
//
//    override fun newDataModel(entity: PartnerEntity): PartnerModel {
//       return entity.toDataModel()
//    }
//
//    override fun copyModel(source: PartnerModel, target: PartnerModel):PartnerModel{
//        var targetCopy = target.copy()
//        targetCopy = source.copy()
//        return targetCopy
//    }
//
//    init {
//        dbQuery {
//            if(!Departments.exists()){
//                SchemaUtils.create(Departments)
//            }
//        }
//        super.initializeService()
//    }
//}
