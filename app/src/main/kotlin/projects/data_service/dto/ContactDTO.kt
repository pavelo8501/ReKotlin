package po.playground.projects.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.initializeDTO
import po.db.data_service.dto.interfaces.DTOModel
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
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
): CommonDTO<ContactDataModel, ContactEntity>(dataModel), DTOModel {

    override val className: String = ""

    companion object : DTOClass<ContactDataModel, ContactEntity>() {
        override fun configuration() {
            initializeDTO<ContactDTO, ContactDataModel, ContactEntity>(ContactEntity) {
               setProperties(
//                   PropertyBinding("name",ContactDataModel::name, ContactEntity::name),
//                   PropertyBinding("surname",ContactDataModel::surname, ContactEntity::surname)
               )
            }
        }
    }

}