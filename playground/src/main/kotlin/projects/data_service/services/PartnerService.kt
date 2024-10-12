package po.playground.projects.data_service.services

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import po.db.data_service.services.BasicDataService
import po.db.data_service.services.models.ServiceDataModel
import po.db.data_service.services.models.ServiceDataModelClass
import po.playground.projects.data_service.DBManager
import po.playground.projects.data_service.services.models.PartnerEntity
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


class PartnerService(override val dbManager: DBManager) : BasicDataService<PartnerModel, PartnerEntity>(PartnerModel,  PartnerEntity){

    override val autoload: Boolean = true
    override val table: LongIdTable = Partners
    override fun newDataModel(entity: PartnerEntity): PartnerModel {
       return entity.toDataModel()
    }

    override fun copyModel(source: PartnerModel, target: PartnerModel):PartnerModel{
        var targetCopy = target.copy()
        targetCopy = source.copy()
        return targetCopy
    }


    init {
        super.initializeService()
    }


}