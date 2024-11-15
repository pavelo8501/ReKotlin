package po.playground.projects.data_service.services.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.services.models.ContainerModel






//@Serializable
//data class DepartmentModel(
//    override var id: Long,
//    @SerialName("partner_id")
//    var partnerId : Long,
//    var hq : Boolean,
//    var name : String,
//    var frequency: Int,
//    var street : String? =null,
//    var city : String? = null,
//    var country : String? = null,
//    @SerialName("post_code")
//    var postCode : String? = null,
//    var phone : String? = null,
//    var email : String? = null,
//    @SerialName("last_inspection")
//    val lastInspection : LocalDateTime? = null,
//    ): ServiceDataModel<DepartmentEntity>(){
//    companion object : ServiceDataModelClass<DepartmentModel, DepartmentEntity>(DepartmentEntity)
//
//    var onPropertyUpdated : ((DepartmentModel)-> Unit)? = null
//    var created: LocalDateTime = Departments.nowDateTime
//    var updated: LocalDateTime =  Departments.nowDateTime
//
//    @Transient
//    override var parentEntityId : EntityID<Long>? = null
//
//
//    @Transient
//    override var sourceEntity: DepartmentEntity? = null
//
//    override fun updateEntity() = listOf(
//        { sourceEntity?.name = updateString(this.name) },
//        { sourceEntity?.hq  =  updateBoolean(this.hq) },
//        { sourceEntity?.street = updateStringOrNull(this.street) },
//        { sourceEntity?.city = updateStringOrNull(this.city) },
//        { sourceEntity?.country = updateStringOrNull(this.country) },
//        { sourceEntity?.postCode = updateStringOrNull(this.postCode) },
//        { sourceEntity?.phone = updateStringOrNull(this.phone) },
//        { sourceEntity?.email = updateStringOrNull(this.email) },
//        { sourceEntity?.frequency = updateInt(this.frequency)?:12 },
//        { sourceEntity?.partner = updateParentEntityId(this.parentEntityId!!) },
//    )
//
//
//}