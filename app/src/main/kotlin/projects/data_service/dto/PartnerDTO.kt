package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.annotations.ClassBinder
import po.db.data_service.annotations.PropertyBinder
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.dao.EntityDAO
import po.db.data_service.dto.*
import po.playground.projects.data_service.dto.Partner.Companion
import po.playground.projects.data_service.services.Partners


@ClassBinder("Partner")
class PartnerEntity  (id: EntityID<Long>) : LongEntity(id), EntityDAO<Partner, PartnerEntity> {
    companion object : LongEntityClass<PartnerEntity>(Partners)

    override var entityDao: EntityDAO<Partner, PartnerEntity> =  this

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

//class Partner(
//    override var id: Long,
//    var name: String,
//    var legalName: String,
//    var regNr: String? = null,
//    var vatNr: String? = null,
//    var updated: LocalDateTime,
//    var created: LocalDateTime,
//): AbstractDTOModel<Partner, PartnerEntity>(Partner), DataModel<PartnerEntity>{
//
//    companion object : DTOClass<Partner, PartnerEntity>(PartnerEntity) {
//        override fun configuration() {
//            config<Partner>{
//                setProperties(
//                    PropertyBinding("name", Partner::name, PartnerEntity::name),
//                    PropertyBinding("legalName",Partner::legalName, PartnerEntity::legalName),
//                    PropertyBinding("regNr", Partner::regNr, PartnerEntity::regNr),
//                    PropertyBinding("vatNr", Partner::vatNr, PartnerEntity::vatNr),
//                    PropertyBinding("updated", Partner::updated, PartnerEntity::updated),
//                    PropertyBinding("created", Partner::created, PartnerEntity::created)
//                )
//            }
//        }
//    }
//}

data class PartnerDataModel(
    var id: Long,
    var name: String,
    var legalName: String,
    var regNr: String? = null,
    var vatNr: String? = null,
    var updated: LocalDateTime,
    var created: LocalDateTime,
)

class PartnerDTO(
    override var id: Long,
    override val dataModel: PartnerDataModel,
): DTOCommon<PartnerDataModel, PartnerEntity>(dataModel), DataModel<PartnerEntity>{

    companion object : DTOClass<PartnerDataModel, PartnerEntity>(PartnerEntity){
        override fun configuration() {
            config<PartnerDataModel> {
                setProperties(
                    PropertyBinding("name", PartnerDataModel::name, PartnerEntity::name),
                    PropertyBinding("legalName", PartnerDataModel::legalName, PartnerEntity::legalName),
                    PropertyBinding("regNr", PartnerDataModel::regNr, PartnerEntity::regNr),
                    PropertyBinding("vatNr", PartnerDataModel::vatNr, PartnerEntity::vatNr),
                    PropertyBinding("updated", PartnerDataModel::updated, PartnerEntity::updated),
                    PropertyBinding("created", PartnerDataModel::created, PartnerEntity::created)
                )
            }
        }
    }

    override fun mapToEntity(entity: PartnerEntity): PartnerEntity {
//        entity.name = data.name
//        entity.legalName = data.legalName
//        entity.regNr = data.regNr
//        entity.vatNr = data.vatNr
//        entity.updated = data.updated
//        entity.created = data.created
//        return entity
        return entity
    }

    override fun mapFromEntity(entity: PartnerEntity): PartnerDataModel {
        return PartnerDataModel(
            id = entity.id.value,
            name = entity.name,
            legalName = entity.legalName,
            regNr = entity.regNr,
            vatNr = entity.vatNr,
            updated = entity.updated,
            created = entity.created
        )
    }

}


