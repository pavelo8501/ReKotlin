package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.binder.BindPropertyClass
import po.db.data_service.binder.DTOBinderClass
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.EntityDTO
import po.db.data_service.dto.ModelDTOContext
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
data class DepartmentDTO(
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
): EntityDTO<DepartmentDTO, DepartmentEntity>(DepartmentDTO,DepartmentEntity), ModelDTOContext{

    companion object : DTOClass<DepartmentDTO, DepartmentEntity>(
        DTOBinderClass(
            BindPropertyClass("hq",DepartmentDTO::hq, DepartmentEntity::hq),
            BindPropertyClass("name",DepartmentDTO::name, DepartmentEntity::name),
            BindPropertyClass("street",DepartmentDTO::street, DepartmentEntity::street),
            BindPropertyClass("city",DepartmentDTO::city, DepartmentEntity::city),
            BindPropertyClass("country",DepartmentDTO::country, DepartmentEntity::country),
            BindPropertyClass("postCode",DepartmentDTO::postCode, DepartmentEntity::postCode),
            BindPropertyClass("phone",DepartmentDTO::phone, DepartmentEntity::phone),
            BindPropertyClass("email",DepartmentDTO::email, DepartmentEntity::email),
            BindPropertyClass("frequency",DepartmentDTO::frequency, DepartmentEntity::frequency),
            BindPropertyClass("lastInspection",DepartmentDTO::lastInspection, DepartmentEntity::lastInspection),
            BindPropertyClass("updated",DepartmentDTO::updated, DepartmentEntity::updated),
            BindPropertyClass("created",DepartmentDTO::created, DepartmentEntity::created),
        )
    )



}