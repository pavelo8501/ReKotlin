package po.playground.projects.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DTOModelV2
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTOV2
import po.playground.projects.data_service.services.Contacts


class ContactEntity  (id: EntityID<Long>) : LongEntity(id){
    companion object : LongEntityClass<ContactEntity>(Contacts)
    var name by Contacts.name
    var surname by Contacts.surname
}

data class ContactDataModel(
    override var id:Long,
    val name: String,
    val surname: String,
) : DataModel


class ContactDTO(
    override var id: Long,
    override val dataModel: ContactDataModel,
): CommonDTOV2(dataModel), DTOModelV2 {

    override var className: String = "ContactDTO"

    companion object: DTOClass() {
        override fun setup() {
            dtoSettings<ContactDTO, ContactDataModel>(ContactEntity){
//                propertyBindings(
//                  //  PropertyBindingV2("name",  ContactDataModel::name, ContactEntity::name),
//                    //PropertyBindingV2("surname", ContactDataModel::surname, ContactEntity::surname)
//                )
            }
        }
    }
}