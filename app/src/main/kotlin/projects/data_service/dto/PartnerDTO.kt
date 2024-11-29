package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
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
class PartnerEntity  (id: EntityID<Long>) : LongEntity(id), EntityDAO<Partner, PartnerEntity> {

    companion object : LongEntityClass<PartnerEntity>(Partners)

    var entityDAO: EntityDAO<Partner, PartnerEntity>  = this

 //   var entityDaoClass: EntityDAO<Partner, PartnerEntity =

    override fun initialize(
        daoEntity: LongEntityClass<PartnerEntity>,
        dataTransferObject: AbstractDTOModel<Partner, PartnerEntity>
    ){
        super.initialize(daoEntity, dataTransferObject)
    }

    override var entityDao: EntityDAO<Partner, PartnerEntity> =  this

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

class Partner(
    override var id: Long,
    var name: String,
    var legalName: String,
    var regNr: String? = null,
    var vatNr: String? = null,
    var updated: LocalDateTime,
    var created: LocalDateTime
): AbstractDTOModel<Partner, PartnerEntity>(Partner),DataModel{

   companion object : DTOClass<Partner, PartnerEntity>()

   init {
        config {
            setProperties(
                PropertyBinding("name", Partner::name, PartnerEntity::name),
                PropertyBinding("legalName",Partner::legalName, PartnerEntity::legalName),
                PropertyBinding("regNr", Partner::regNr, PartnerEntity::regNr),
                PropertyBinding("vatNr", Partner::vatNr, PartnerEntity::vatNr),
                PropertyBinding("updated", Partner::updated, PartnerEntity::updated),
                PropertyBinding("created", Partner::created, PartnerEntity::created)
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