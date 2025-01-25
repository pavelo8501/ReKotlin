package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.classes.DTOClass
import po.db.data_service.classes.interfaces.DTOModel
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.dto.CommonDTO


import po.playground.projects.data_service.services.Departments
import po.playground.projects.data_service.services.Inspections

class DepartmentEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<DepartmentEntity>(Departments)
    var hq by Departments.hq
    var name by Departments.name
    var street by Departments.street
    var city by Departments.city
    var country by Departments.country
    var postCode by Departments.postCode
    var phone by Departments.phone
    var email by Departments.email
    var frequency by Departments.frequency
    var lastInspection by Departments.lastInspection
    var created by Departments.created
    var updated by Departments.updated
    var partner by PartnerEntity referencedOn Departments.partner
    val inspections by  InspectionEntity referrersOn Inspections.department
}

@Serializable
data class DepartmentDataModel(
    var hq: Boolean,
    var name: String,
    var frequency: Int,
    var street: String? = null,
    var city: String? = null,
    var country: String? = null,
    var postCode: String? = null,
    var phone: String? = null,
    var email: String? = null,
    var lastInspection: LocalDateTime? = null,
): DataModel{
    override var id: Long = 0L
    var updated: LocalDateTime = DepartmentDTO.nowTime()
    var created: LocalDateTime = DepartmentDTO.nowTime()

    val inspections = mutableListOf<InspectionDataModel>()

}

class DepartmentDTO(
    override val dataModel: DepartmentDataModel,
): CommonDTO<DepartmentDataModel, DepartmentEntity>(dataModel), DTOModel{

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
                setDataModelConstructor {
                    DepartmentDataModel(false, "", 12)
                }
                childBindings{
                    childBinding<InspectionDataModel, InspectionEntity>(
                        InspectionDTO,
                        DepartmentEntity::inspections,
                        InspectionEntity::department,
                        DepartmentDataModel::inspections
                    )
                }
            }
        }
    }
}