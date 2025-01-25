package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.classes.DTOClass
import po.db.data_service.classes.interfaces.DTOModel
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.dto.CommonDTO
import po.playground.projects.data_service.services.Contacts
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
    val contact by ContactEntity optionalBackReferencedOn Contacts.partner
}


@Serializable
data class PartnerDataModel(
    var name: String,
    var legalName: String,
    var regNr: String? = null,
    var vatNr: String? = null,
): DataModel{
    override var id: Long = 0L
    var updated: LocalDateTime = PartnerDTO.nowTime()
    var created: LocalDateTime = PartnerDTO.nowTime()
    val departments = mutableListOf<DepartmentDataModel>()
    var contact : ContactDataModel? = null
}

class PartnerDTO(
    override val dataModel: PartnerDataModel,
): CommonDTO<PartnerDataModel, PartnerEntity>(dataModel), DTOModel{

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

                childBindings{
                    childBinding<ContactDataModel, ContactEntity>(
                        ContactDTO,
                        PartnerEntity::contact,
                        ContactEntity::partner,
                        PartnerDataModel::contact
                    )
                    childBinding<DepartmentDataModel, DepartmentEntity>(
                        DepartmentDTO,
                        PartnerEntity::departments,
                        DepartmentEntity::partner,
                        PartnerDataModel::departments
                    )
                }
            }
        }
    }
}



