package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.annotations.ClassBinder
import po.db.data_service.annotations.PropertyBinder
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.dto.*


import po.playground.projects.data_service.services.Departments

@ClassBinder("Department")
class DepartmentEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DepartmentEntity>(Departments)
    @PropertyBinder("hq")
    var hq by Departments.hq
    @PropertyBinder("name")
    var name by Departments.name
    @PropertyBinder("street")
    var street by Departments.street
    @PropertyBinder("city")
    var city by Departments.city
    @PropertyBinder("country")
    var country by Departments.country
    @PropertyBinder("postCode")
    var postCode by Departments.postCode
    @PropertyBinder("phone")
    var phone by Departments.phone
    @PropertyBinder("email")
    var email by Departments.email
    @PropertyBinder("frequency")
    var frequency by Departments.frequency
    @PropertyBinder("lastInspection")
    var lastInspection by Departments.lastInspection
    @PropertyBinder("created")
    var created by Departments.created
    @PropertyBinder("updated")
    var updated by Departments.updated
    var partner by PartnerEntity referencedOn Departments.partner
}

@ClassBinder("Department")
data class DepartmentDataModel(
    override var id: Long,
    @PropertyBinder("hq")
    var hq: Boolean,
    @PropertyBinder("name")
    var name: String,
    @PropertyBinder("street")
    var street: String? = null,
    @PropertyBinder("city")
    var city: String? = null,
    @PropertyBinder("country")
    var country: String? = null,
    @PropertyBinder("postCode")
    var postCode: String? = null,
    @PropertyBinder("phone")
    var phone: String? = null,
    @PropertyBinder("email")
    var email: String? = null,
    @PropertyBinder("frequency")
    var frequency: Int,
    @PropertyBinder("lastInspection")
    var lastInspection: LocalDateTime? = null,
    @PropertyBinder("updated")
    var updated: LocalDateTime,
    @PropertyBinder("created")
    var created: LocalDateTime,
):DataModel

class DepartmentDTO(
    override var id: Long,
    override val dataModel: DepartmentDataModel,
): CommonDTO<DepartmentDataModel, DepartmentEntity>(dataModel), DTOModel{

    companion object : DTOClass<DepartmentDataModel, DepartmentEntity>(){
        override fun configuration() {
            initializeDTO<DepartmentDTO, DepartmentDataModel, DepartmentEntity>(DepartmentEntity) {
                setProperties(
                    PropertyBinding("hq",DepartmentDataModel::hq, DepartmentEntity::hq),
                    PropertyBinding("name",DepartmentDataModel::name, DepartmentEntity::name),
                    PropertyBinding("street",DepartmentDataModel::street, DepartmentEntity::street),
                    PropertyBinding("city",DepartmentDataModel::city, DepartmentEntity::city),
                    PropertyBinding("country",DepartmentDataModel::country, DepartmentEntity::country),
                    PropertyBinding("postCode",DepartmentDataModel::postCode, DepartmentEntity::postCode),
                    PropertyBinding("phone",DepartmentDataModel::phone, DepartmentEntity::phone),
                    PropertyBinding("email",DepartmentDataModel::email, DepartmentEntity::email),
                    PropertyBinding("frequency",DepartmentDataModel::frequency, DepartmentEntity::frequency),
                    PropertyBinding("lastInspection",DepartmentDataModel::lastInspection, DepartmentEntity::lastInspection),
                    PropertyBinding("updated",DepartmentDataModel::updated, DepartmentEntity::updated),
                    PropertyBinding("created",DepartmentDataModel::created, DepartmentEntity::created),
                )
            }
        }
    }
}