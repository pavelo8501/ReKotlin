package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.annotations.ClassBinder
import po.db.data_service.annotations.PropertyBinder
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.dto.*
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO


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
    var hq: Boolean,
    var name: String,
    var street: String? = null,
    var city: String? = null,
    var country: String? = null,
    var postCode: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var frequency: Int,
    var lastInspection: LocalDateTime? = null,
    var updated: LocalDateTime,
    var created: LocalDateTime,
): DataModel


class DepartmentDTO(
    override var id: Long,
    override val dataModel: DepartmentDataModel,
): CommonDTO(dataModel){

    override var className: String = "DepartmentDTOV2"

    companion object: DTOClass<DepartmentEntity>() {
        override fun setup() {
            dtoSettings<DepartmentDTO, DepartmentDataModel>(DepartmentEntity){
                propertyBindings(
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
                setDataModelConstructor{
                    DepartmentDataModel(0,false,"",null,null,null,null,null,null,12, null, nowTime(), nowTime() )
                }
            }
        }
    }

}