package po.playground.projects.data_service.services.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.services.models.ChildMapping
import po.db.data_service.services.models.ServiceDBEntity
import po.db.data_service.services.models.ServiceDataModel
import po.db.data_service.services.models.ServiceDataModelClass
import po.playground.projects.data_service.services.Departments

class DepartmentEntity(id: EntityID<Long>) : ServiceDBEntity(id) {
    companion object : LongEntityClass<DepartmentEntity>(Departments)

    var partner: EntityID<Long> by Departments.partner
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


    fun toDataModel(): DepartmentModel {
        return DepartmentModel(
            id.value,
            partner.value,
            hq,
            name,
            frequency,
            street,
            city,
            country,
            postCode,
            phone,
            email,
            lastInspection
        ).also {
            it.created = created
            it.updated = updated
        }
    }
}


@Serializable
data class DepartmentModel(
    override var id: Long,
    @SerialName("partner_id")
    var partnerId : Long,
    var hq : Boolean,
    var name : String,
    var frequency: Int,
    var street : String? =null,
    var city : String? = null,
    var country : String? = null,
    @SerialName("post_code")
    var postCode : String? = null,
    var phone : String? = null,
    var email : String? = null,
    @SerialName("last_inspection")
    val lastInspection : LocalDateTime? = null,
    ): ServiceDataModel<DepartmentEntity>(){
    companion object : ServiceDataModelClass<DepartmentModel, DepartmentEntity>(DepartmentEntity)

    var onPropertyUpdated : ((DepartmentModel)-> Unit)? = null
    var created: LocalDateTime = Departments.nowDateTime
    var updated: LocalDateTime =  Departments.nowDateTime

    @Transient
    override var parentEntityId : EntityID<Long>? = null


    @Transient
    override var sourceEntity: DepartmentEntity? = null

    override fun updateEntity() = listOf(
        { sourceEntity?.name = updateString(this.name) },
        { sourceEntity?.hq  =  updateBoolean(this.hq) },
        { sourceEntity?.street = updateStringOrNull(this.street) },
        { sourceEntity?.city = updateStringOrNull(this.city) },
        { sourceEntity?.country = updateStringOrNull(this.country) },
        { sourceEntity?.postCode = updateStringOrNull(this.postCode) },
        { sourceEntity?.phone = updateStringOrNull(this.phone) },
        { sourceEntity?.email = updateStringOrNull(this.email) },
        { sourceEntity?.frequency = updateInt(this.frequency)?:12 },
        { sourceEntity?.partner = updateParentEntityId(this.parentEntityId!!) },
    )


}