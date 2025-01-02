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
import po.db.data_service.models.EntityDTO


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

sealed interface HierarchyMember : HierarchyBase {
    val className : String
}


class DepartmentDTO(
    override val dataModel: DepartmentDataModel,
): EntityDTO<DepartmentDataModel, DepartmentEntity>(dataModel), HierarchyMember{

    override var className: String = "DepartmentDTOV2"

    companion object: DTOClass<DepartmentDataModel, DepartmentEntity>(DepartmentDTO::class) {
        override fun setup() {
            dtoSettings<DepartmentDataModel, DepartmentEntity>(DepartmentEntity){
                propertyBindings(
                    PropertyBinding(DepartmentDataModel::hq, DepartmentEntity::hq),
                    PropertyBinding(DepartmentDataModel::name, DepartmentEntity::name),
                    PropertyBinding(DepartmentDataModel::street, DepartmentEntity::street),
                    PropertyBinding(DepartmentDataModel::city, DepartmentEntity::city),
                    PropertyBinding(DepartmentDataModel::country, DepartmentEntity::country),
                    PropertyBinding(DepartmentDataModel::postCode, DepartmentEntity::postCode),
                    PropertyBinding(DepartmentDataModel::phone, DepartmentEntity::phone),
                    PropertyBinding(DepartmentDataModel::email, DepartmentEntity::email),
                    PropertyBinding(DepartmentDataModel::frequency, DepartmentEntity::frequency),
                    PropertyBinding(DepartmentDataModel::lastInspection, DepartmentEntity::lastInspection),
                    PropertyBinding(DepartmentDataModel::updated, DepartmentEntity::updated),
                    PropertyBinding(DepartmentDataModel::created, DepartmentEntity::created),
                )
                setDataModelConstructor{
                    DepartmentDataModel(0, false,"",null,null,null,null,null,null,12, null, nowTime(), nowTime() )
                }
            }
        }
    }
}