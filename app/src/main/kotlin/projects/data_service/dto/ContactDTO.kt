package po.playground.projects.data_service.dto

import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DTOModel
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import po.playground.projects.data_service.services.ContactEntity


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
            val config2 = "configurationContactDTO"
        }
    }
}