package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.dto.*
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.EntityDTO
import po.playground.projects.data_service.services.Departments
import po.playground.projects.data_service.services.Partners

class PartnerEntity  (id: EntityID<Long>) : LongEntity(id){
    companion object : LongEntityClass<PartnerEntity>(Partners)
    var name by Partners.name
    var legalName by Partners.legalName
    var regNr by Partners.regNr
    var vatNr by Partners.vatNr
    var created by Partners.created
    var updated by Partners.updated
    val departments by  DepartmentEntity referrersOn Departments.partner
}

data class PartnerDataModel(
    override var id: Long,
    var name: String,
    var legalName: String,
    var regNr: String? = null,
    var vatNr: String? = null,
    var updated: LocalDateTime,
    var created: LocalDateTime,
): DataModel{
    var departments = mutableListOf<DepartmentDataModel>()
}

class PartnerDTO(
    override val dataModel: PartnerDataModel,
): EntityDTO<PartnerDataModel, PartnerEntity>(dataModel){
    override var className: String = "PartnerDTO"


    companion object: DTOClass<PartnerDataModel, PartnerEntity>(PartnerDTO::class) {
         override fun setup() {
            dtoSettings<PartnerDataModel, PartnerEntity>(PartnerEntity){
                propertyBindings(
                    PropertyBinding(PartnerDataModel::name, PartnerEntity::name),
                    PropertyBinding(PartnerDataModel::legalName, PartnerEntity::legalName),
                    PropertyBinding(PartnerDataModel::regNr, PartnerEntity::regNr),
                    PropertyBinding(PartnerDataModel::vatNr, PartnerEntity::vatNr),
                    PropertyBinding( PartnerDataModel::updated, PartnerEntity::updated),
                    PropertyBinding( PartnerDataModel::created, PartnerEntity::created)
                )
                childBindings<DepartmentDataModel, DepartmentEntity>(DepartmentDTO,PartnerEntity::departments, DepartmentEntity::partner)
            }
        }
    }
}



