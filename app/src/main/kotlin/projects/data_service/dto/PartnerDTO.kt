package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.annotations.ClassBinder
import po.db.data_service.annotations.PropertyBinder
import po.db.data_service.binder.OrdinanceType
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.dto.*
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import po.playground.projects.data_service.services.Departments
import po.playground.projects.data_service.services.Partners

@ClassBinder("Partner")
class PartnerEntity  (id: EntityID<Long>) : LongEntity(id){
    companion object : LongEntityClass<PartnerEntity>(Partners)
    @PropertyBinder("name")
    var name by Partners.name
    @PropertyBinder("legalName")
    var legalName by Partners.legalName
    @PropertyBinder("regNr")
    var regNr by Partners.regNr
    @PropertyBinder("vatNr")
    var vatNr by Partners.vatNr
    @PropertyBinder("created")
    var created by Partners.created
    @PropertyBinder("updated")
    var updated by Partners.updated

    val departments by  DepartmentEntity referrersOn Departments.partner
}

data class PartnerDataModel(
    var name: String,
    var legalName: String,
    var regNr: String? = null,
    var vatNr: String? = null,
): DataModel{
    override var id: Long = 0
    var updated: LocalDateTime = PartnerDTO.nowTime()
    var created: LocalDateTime = PartnerDTO.nowTime()
    val departments = mutableListOf<DepartmentDataModel>()
}

class PartnerDTO(
    override val dataModel: PartnerDataModel,
): CommonDTO(dataModel){
    override var className: String = "PartnerDTO"

    companion object: DTOClass<PartnerEntity>() {
        override fun modelSetup() {
            dtoSettings<PartnerDTO, PartnerDataModel>(PartnerEntity) {
                propertyBindings(
                    PropertyBinding("name", PartnerDataModel::name, PartnerEntity::name),
                    PropertyBinding("legalName", PartnerDataModel::legalName, PartnerEntity::legalName),
                    PropertyBinding("regNr", PartnerDataModel::regNr, PartnerEntity::regNr),
                    PropertyBinding("vatNr", PartnerDataModel::vatNr, PartnerEntity::vatNr),
                    PropertyBinding("updated", PartnerDataModel::updated, PartnerEntity::updated),
                    PropertyBinding("created", PartnerDataModel::created, PartnerEntity::created)
                )
            }
            relationBindings<PartnerDTO>{
                addBinding(DepartmentDTO, PartnerEntity::departments, DepartmentEntity::partner){
                    setDataSource<PartnerDataModel, DepartmentDataModel>(PartnerDataModel::departments){

                    }
                }
            }

        }
    }
}



