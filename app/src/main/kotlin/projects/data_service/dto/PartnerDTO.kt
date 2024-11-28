package po.playground.projects.data_service.dto

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.annotations.ClassBinder
import po.db.data_service.annotations.PropertyBinder
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.dao.EntityDAO
import po.db.data_service.dto.*
import po.playground.projects.data_service.services.Partners


@ClassBinder("Partner")
class PartnerEntity  (id: EntityID<Long>) : LongEntity(id), EntityDAO< PartnerEntity, Partner > {

    companion object : LongEntityClass<PartnerEntity>(Partners)

    override var entityDAO: LongEntityClass<PartnerEntity> = PartnerEntity

    override fun initialize(
        daoEntity: LongEntityClass<PartnerEntity>,
        dataTransferObject: DataTransferObjectsParent<Partner>
    ){
        super.initialize(daoEntity, dataTransferObject)
    }

    @PropertyBinder("name")
    var name by Partners.name
    @PropertyBinder("legalName")
    var legalName by Partners.legalName
    @PropertyBinder("regNr")
    var regNr by Partners.regNr
    @PropertyBinder("vatNr")
    var vatNr by Partners.vatNr
    @PropertyBinder("created")
    var created by Partners.created
    @PropertyBinder("updated")
    var updated by Partners.updated
    //val departments by  DepartmentEntity referrersOn Departments.partne
}

data class Partner(
    override var id: Long,
    var name: String
): DataTransferObjectsParent<Partner>(),MarkerInterface{


    fun companion(): DataTransferObjectsParent<Partner>{
       return  this.companionObject()
    }

    fun initEntityDao(){
       Companion.createModelEntityPair(companion().sysName, companion(), PartnerEntity)
        EntityDAO.pairEntities(PartnerEntity,companion())
    }

    override fun <T> dataTransferModelsConfiguration(body: DTObConfigContext, function: DTObConfigContext.() -> Unit) {

        initEntityDao()

        configureDataTransferObject{
                cretePropertyBindings(
                    PropertyBinding("name", Partner::name,PartnerEntity::name)
                )
            }
    }
}



//@ClassBinder("Partner")
//data class PartnerDTO(
//    override var id: Long,
//    @PropertyBinder("name")
//    var name: String,
//    @PropertyBinder("legalName")
//    var legalName: String,
//    @PropertyBinder("regNr")
//    var regNr: String? = null,
//    @PropertyBinder("vatNr")
//    var vatNr: String? = null,
//    @PropertyBinder("updated")
//    var updated: LocalDateTime,
//    @PropertyBinder("created")
//    var created: LocalDateTime,
//): CommonDTO<PartnerDTO, PartnerEntity>(), ModelDTOContext{
//    val self : PartnerDTO;
//    init {
//        self = this
//    }
//    companion object : DTOClass<PartnerDTO, PartnerEntity>()  {
//
//
//    }
//    val departments: MutableList<DepartmentDTO> = mutableListOf()
//     fun bindings(): ChildClasses<*,*,*,*> {
//        return ChildClasses<DepartmentDTO,DepartmentEntity, PartnerDTO, PartnerEntity>(
//            PartnerDTO,
//            OneToManyBinding<DepartmentDTO,DepartmentEntity,PartnerDTO,PartnerEntity>(DepartmentDTO, PartnerEntity::departments, DepartmentEntity::partner, departments)
//        )
//    }
//     fun updateChild(){
//
//    }
//}




//@ClassBinder("Partner")
//data class PartnerDTO(
//    override var id: Long,
//    @PropertyBinder("name")
//    var name: String,
//    @PropertyBinder("legalName")
//    var legalName: String,
//    @PropertyBinder("regNr")
//    var regNr: String? = null,
//    @PropertyBinder("vatNr")
//    var vatNr: String? = null,
//    @PropertyBinder("updated")
//    var updated: LocalDateTime,
//    @PropertyBinder("created")
//    var created: LocalDateTime,
//): EntityDTO<PartnerDTO, PartnerEntity>(PartnerDTO,PartnerEntity), ParentDTOContext{
//
//    companion object : DTOClass<PartnerDTO, PartnerEntity>(
//
//    )
//
//  val departments: MutableList<DepartmentDTO> = mutableListOf()
//
//  override fun bindings(): ChildClasses<*,*,*,*> {
//     return ChildClasses<DepartmentDTO,DepartmentEntity, PartnerDTO, PartnerEntity>(
//                PartnerDTO,
//                OneToManyBinding<DepartmentDTO,DepartmentEntity,PartnerDTO,PartnerEntity>(DepartmentDTO, PartnerEntity::departments, DepartmentEntity::partner, departments)
//            )
//    }
//
//    override fun updateChild(){
//
//    }
//}