package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.annotations.ClassBinder
import po.db.data_service.annotations.PropertyBinder
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.dto.AbstractDTOModel
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.DataModel


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
data class Department(
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
): AbstractDTOModel<Department, DepartmentEntity>(Department), DataModel {

    override val dataModel: Department = this
    override val sysName = "DepartmentDTO"

    companion object : DTOClass<Department, DepartmentEntity>() {
        override fun configuration() {
            config {
                setProperties(
                    PropertyBinding("hq",Department::hq, DepartmentEntity::hq),
                    PropertyBinding("name",Department::name, DepartmentEntity::name),
                    PropertyBinding("street",Department::street, DepartmentEntity::street),
                    PropertyBinding("city",Department::city, DepartmentEntity::city),
                    PropertyBinding("country",Department::country, DepartmentEntity::country),
                    PropertyBinding("postCode",Department::postCode, DepartmentEntity::postCode),
                    PropertyBinding("phone",Department::phone, DepartmentEntity::phone),
                    PropertyBinding("email",Department::email, DepartmentEntity::email),
                    PropertyBinding("frequency",Department::frequency, DepartmentEntity::frequency),
                    PropertyBinding("lastInspection",Department::lastInspection, DepartmentEntity::lastInspection),
                    PropertyBinding("updated",Department::updated, DepartmentEntity::updated),
                    PropertyBinding("created",Department::created, DepartmentEntity::created),
                )
            }
        }
    }
}