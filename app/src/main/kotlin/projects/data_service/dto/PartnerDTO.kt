package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.annotations.ClassBinder
import po.db.data_service.annotations.PropertyBinder
import po.db.data_service.binder.OrdinanceType
import po.db.data_service.binder.PropertyBindingV2
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
    override var id: Long,
    var name: String,
    var legalName: String,
    var regNr: String? = null,
    var vatNr: String? = null,
    var updated: LocalDateTime,
    var created: LocalDateTime,
): DataModel


class PartnerDTO(
    override var id: Long,
    override val dataModel: PartnerDataModel,
): CommonDTO(dataModel){
    override var className: String = "PartnerDTO"

    companion object: DTOClass() {
        override fun setup() {
            dtoSettings<PartnerDTO, PartnerDataModel>(PartnerEntity){
                propertyBindings(
                    PropertyBindingV2("name", PartnerDataModel::name, PartnerEntity::name),
                    PropertyBindingV2("legalName", PartnerDataModel::legalName, PartnerEntity::legalName),
                    PropertyBindingV2("regNr", PartnerDataModel::regNr, PartnerEntity::regNr),
                    PropertyBindingV2("vatNr", PartnerDataModel::vatNr, PartnerEntity::vatNr),
                    PropertyBindingV2("updated", PartnerDataModel::updated, PartnerEntity::updated),
                    PropertyBindingV2("created", PartnerDataModel::created, PartnerEntity::created)
                )
                childBinding<PartnerEntity, DepartmentEntity>(DepartmentDTOV2, PartnerEntity::departments,  OrdinanceType.ONE_TO_MANY)
            }
        }
    }
}



