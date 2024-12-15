package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.annotations.ClassBinder
import po.db.data_service.annotations.PropertyBinder
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.dto.*
import po.db.data_service.dto.components.BindingContainer
import po.db.data_service.dto.components.BindingType
import po.db.data_service.dto.components.RelationBinder
import po.db.data_service.dto.interfaces.DTOModel
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
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
    //val departments by  DepartmentEntity referrersOn Departments.partne
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
): CommonDTO<PartnerDataModel, PartnerEntity>(dataModel), DTOModel {

    override val className: String = "PartnerDTO"

    companion object : DTOClass<PartnerDataModel, PartnerEntity>() {
        override fun configuration(){
            initializeDTO<PartnerDataModel, PartnerEntity>(PartnerEntity){
                setProperties(
                    PropertyBinding("name", PartnerDataModel::name, PartnerEntity::name),
                    PropertyBinding("legalName", PartnerDataModel::legalName, PartnerEntity::legalName),
                    PropertyBinding("regNr", PartnerDataModel::regNr, PartnerEntity::regNr),
                    PropertyBinding("vatNr", PartnerDataModel::vatNr, PartnerEntity::vatNr),
                    PropertyBinding("updated", PartnerDataModel::updated, PartnerEntity::updated),
                    PropertyBinding("created", PartnerDataModel::created, PartnerEntity::created)
                )
                setDataModelConstructor {
                    PartnerDataModel(0,"","",null, null, nowTime(), nowTime())
                }

            }
        }
    }
}


