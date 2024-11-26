package po.playground.projects.data_service.dto

import kotlinx.datetime.LocalDateTime
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.binder.*
import po.db.data_service.dto.*
import po.playground.projects.data_service.services.Departments
import po.playground.projects.data_service.services.Partners
import kotlin.Long

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ClassBinder(val key: String)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class PropertyBinder (val key: String = "")


@ClassBinder("Partner")
class PartnerEntity(id: EntityID<Long>) : LongEntity(id){
    companion object : LongEntityClass<PartnerEntity>(Partners)
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

  //  val departments by  DepartmentEntity referrersOn Departments.partner

}




@ClassBinder("Partner")
data class PartnerDTO(
    override var id: Long,
    @PropertyBinder("name")
    var name: String,
    @PropertyBinder("legalName")
    var legalName: String,
    @PropertyBinder("regNr")
    var regNr: String? = null,
    @PropertyBinder("vatNr")
    var vatNr: String? = null,
    @PropertyBinder("updated")
    var updated: LocalDateTime,
    @PropertyBinder("created")
    var created: LocalDateTime,
): CommonDTO<PartnerDTO, PartnerEntity>(), ModelDTOContext{

    companion object : DTOClass<PartnerEntity>(PartnerEntity,
        DTOBinder<PartnerEntity>
    )

    //        DTOBinder(this,
//            PropertyBinding("name",PartnerNewDTO::name ,PartnerEntity::name),
//            PropertyBinding("legalName",PartnerNewDTO::legalName ,PartnerEntity::legalName),
//            PropertyBinding("regNr",PartnerNewDTO::regNr ,PartnerEntity::regNr),
//            PropertyBinding("vatNr",PartnerNewDTO::vatNr ,PartnerEntity::vatNr),
//            PropertyBinding("updated",PartnerNewDTO::updated ,PartnerEntity::updated),
//            PropertyBinding("created",PartnerNewDTO::created ,PartnerEntity::created),
//        )

    override val entityClass: LongEntityClass<PartnerEntity> = null

    val departments: MutableList<DepartmentDTO> = mutableListOf()

     fun bindings(): ChildClasses<*,*,*,*> {
        return ChildClasses<DepartmentDTO,DepartmentEntity, PartnerDTO, PartnerEntity>(
            PartnerDTO,
            OneToManyBinding<DepartmentDTO,DepartmentEntity,PartnerDTO,PartnerEntity>(DepartmentDTO, PartnerEntity::departments, DepartmentEntity::partner, departments)
        )
    }

     fun updateChild(){

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