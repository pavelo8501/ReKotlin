package po.playground.projects.data_service.services.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Contextual
import kotlinx.serialization.*
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.annotations.BindProperty
import po.db.data_service.services.models.ServiceDBEntity
import po.db.data_service.services.models.ServiceDataModel
import po.db.data_service.services.models.ServiceDataModelClass
import po.db.data_service.services.models.ChildMapping
import po.db.data_service.services.models.ContainerModel
import po.db.data_service.services.models.CoreDbEntity
import po.db.data_service.services.models.CoreDbEntityContext
import po.playground.projects.data_service.services.Departments
import po.playground.projects.data_service.services.PartnerEntity
import po.playground.projects.data_service.services.Partners


class PartnerModel(
    override var id: Long,
    @BindProperty("name")
    var name : String,
    @SerialName("legal_name")
    @BindProperty("legalName")
    var legalName: String,
    @SerialName("reg_nr")
    @BindProperty("regNr")
    val regNr : String? = null,
    @SerialName("vat_nr")
    @BindProperty("vatNr")
    val vatNr : String? = null,
    ): ContainerModel<PartnerEntity>(PartnerEntity::class) {


}

//@Serializable
//data class PartnerModel(
//    override var id: Long,
//    var name : String,
//    @SerialName("legal_name")
//    var legalName: String,
//    @SerialName("reg_nr")
//    val regNr : String? = null,
//    @SerialName("vat_nr")
//    val vatNr : String? = null,
//) : ServiceDataModel<PartnerEntity>() {
//    companion object : ServiceDataModelClass<PartnerModel, PartnerEntity>(PartnerEntity)
//
//    val departments : MutableList<DepartmentModel> = mutableListOf()
//
//    override val childMapping: List<ChildMapping<out ServiceDataModel<*>, out ServiceDBEntity>> by lazy {
//        listOf(
//            ChildMapping(DepartmentModel, departments, "department", sourceEntity?.id)
//        )
//    }
//
//    init {
//       //val newMapping = ChildMapping<DepartmentModel, DepartmentEntity>(DepartmentModel, departments, "department")
//     //  childMapping2.add(newMapping)
//      //  childMappings.add(newMapping)
//    }
//
//    var created: LocalDateTime? = nowDateTime
//    var updated: LocalDateTime? = nowDateTime
//
//    @Transient
//    override var sourceEntity: PartnerEntity? = null
//
//    @Transient
//    override var parentEntityId: EntityID<Long>? = null
//
//    override fun updateEntity() = listOf(
//        { sourceEntity?.name = updateString(this.name) },
//        { sourceEntity?.legalName = updateString(this.legalName) },
//        { sourceEntity?.regNr = updateStringOrNull(this.regNr) },
//        { sourceEntity?.vatNr = updateStringOrNull(this.vatNr) },
//    )
//
//    override fun setEntity(entity: PartnerEntity) {
//        super.setEntity(entity)
//        // Refresh child mapping with the new parentEntityId (if necessary)
//        childMapping.forEach { it.parentEntityId = entity.id }
//    }
//
//}