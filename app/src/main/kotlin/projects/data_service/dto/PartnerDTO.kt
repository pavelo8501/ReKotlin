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

    val departments by  DepartmentEntity referrersOn Departments.partner

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
): EntityDTO<PartnerDTO, PartnerEntity>(PartnerDTO,PartnerEntity), ModelDTOContext{

    companion object : DTOClass<PartnerDTO, PartnerEntity>(
            DTOBinderClass(
                BindPropertyClass("name",PartnerDTO::name ,PartnerEntity::name),
                BindPropertyClass("legalName",PartnerDTO::legalName ,PartnerEntity::legalName),
                BindPropertyClass("regNr",PartnerDTO::regNr ,PartnerEntity::regNr),
                BindPropertyClass("vatNr",PartnerDTO::vatNr ,PartnerEntity::vatNr),
                BindPropertyClass("updated",PartnerDTO::updated ,PartnerEntity::updated),
                BindPropertyClass("created",PartnerDTO::created ,PartnerEntity::created),
            )
    )


}