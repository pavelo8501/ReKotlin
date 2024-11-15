package po.playground.projects.data_service.services.models

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.db.data_service.services.models.ContainerModel

//class PartnerModel(
//    override var id: Long,
//    var name : String,
//    var legalName: String,
//    var regNr : String? = null,
//    var vatNr : String? = null,
//    ): ContainerModel<LongEntityClass<LongEntity>>(PartnerEntity) {
//        init {
//            val a = 10
//        }
//        override fun initialize(){
//            childBindings {
//              // childContainer<DepartmentModel, PartnerEntity>(DepartmentModel::class, PartnerEntity::departments, false)
//            }
//        }
//}

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
//      // Refresh child mapping with the new parentEntityId (if necessary)
//        childMapping.forEach { it.parentEntityId = entity.id }
//    }
//
//}