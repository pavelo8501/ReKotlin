package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.annotations.ClassBinder
import po.db.data_service.annotations.PropertyBinder
import po.db.data_service.binder.PropertyBindingV2
import po.db.data_service.dto.*
import po.db.data_service.dto.interfaces.DTOModelV2
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTOV2


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


class DepartmentDTOV2(
    override var id: Long,
    override val dataModel: DepartmentDataModel,
): CommonDTOV2(dataModel), DTOModelV2 {

    override var className: String = "DepartmentDTOV2"

    companion object: DTOClass() {
        override fun setup() {
            dtoSettings<DepartmentDTOV2, DepartmentDataModel>(DepartmentEntity){
                propertyBindings(
                    PropertyBindingV2("hq",DepartmentDataModel::hq, DepartmentEntity::hq),
                    PropertyBindingV2("name",DepartmentDataModel::name, DepartmentEntity::name),
                    PropertyBindingV2("street",DepartmentDataModel::street, DepartmentEntity::street),
                    PropertyBindingV2("city",DepartmentDataModel::city, DepartmentEntity::city),
                    PropertyBindingV2("country",DepartmentDataModel::country, DepartmentEntity::country),
                    PropertyBindingV2("postCode",DepartmentDataModel::postCode, DepartmentEntity::postCode),
                    PropertyBindingV2("phone",DepartmentDataModel::phone, DepartmentEntity::phone),
                    PropertyBindingV2("email",DepartmentDataModel::email, DepartmentEntity::email),
                    PropertyBindingV2("frequency",DepartmentDataModel::frequency, DepartmentEntity::frequency),
                    PropertyBindingV2("lastInspection",DepartmentDataModel::lastInspection, DepartmentEntity::lastInspection),
                    PropertyBindingV2("updated",DepartmentDataModel::updated, DepartmentEntity::updated),
                    PropertyBindingV2("created",DepartmentDataModel::created, DepartmentEntity::created),
                )
                setDataModelConstructor{
                    DepartmentDataModel(0,false,"",null,null,null,null,null,null,12, null, nowTime(), nowTime() )
                }
            }
        }
    }

}