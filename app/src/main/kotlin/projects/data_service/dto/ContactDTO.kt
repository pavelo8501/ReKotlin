package po.playground.projects.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.CommonDTO2
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
    override val dataModel: ContactDataModel,
): CommonDTO2(dataModel){

    override var className: String = "ContactDTO"

    companion object: DTOClass<ContactEntity>(ContactEntity::class) {
        override fun modelSetup() {
            dtoSettings<ContactDTO, ContactDataModel>(ContactEntity){
//                propertyBindings(
//                  //  PropertyBindingV2("name", ContactDataModel::name, ContactEntity::name),
//                   // PropertyBindingV2("surname", ContactDataModel::surname, ContactEntity::surname)
//                )
            }
        }
    }
}