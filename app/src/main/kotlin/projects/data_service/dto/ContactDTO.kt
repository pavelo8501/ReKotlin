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


class ContactEntity  (id: EntityID<Long>) : LongEntity(id){
    companion object : LongEntityClass<ContactEntity>(Contacts)
    var dds by Contacts.dds
    var name by Contacts.name
    var surname by Contacts.surname
    var position by Contacts.position
    var phone by Contacts.phone
    var email by Contacts.email
    var created by Contacts.created
    var updated by Contacts.updated
    var partner by PartnerEntity referencedOn Contacts.partner
}

@Serializable
data class ContactDataModel(
    var dds: Boolean,
    var name: String,
    var surname: String? = null,
    var position: String? = null,
    var phone: String? = null,
    var email: String? = null,
): DataModel{
    override var id: Long = 0L
    var updated: LocalDateTime = DepartmentDTO.nowTime()
    var created: LocalDateTime = DepartmentDTO.nowTime()
}

class ContactDTO(
    override val dataModel: ContactDataModel,
): CommonDTO<ContactDataModel, ContactEntity>(dataModel), DTOModel{

    companion object: DTOClass<ContactDataModel, ContactEntity>(ContactDTO::class) {
        override fun setup() {
            dtoSettings<ContactDataModel, ContactEntity>(ContactEntity){
                propertyBindings(
                    PropertyBinding(ContactDataModel::name, ContactEntity::name),
                    PropertyBinding(ContactDataModel::dds, ContactEntity::dds),
                    PropertyBinding(ContactDataModel::surname, ContactEntity::surname),
                    PropertyBinding(ContactDataModel::position, ContactEntity::position),
                    PropertyBinding(ContactDataModel::phone, ContactEntity::phone),
                    PropertyBinding(ContactDataModel::email, ContactEntity::email)
                )
            }
        }
    }
}

