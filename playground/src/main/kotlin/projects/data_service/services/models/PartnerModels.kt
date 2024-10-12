package po.playground.projects.data_service.services.models

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import po.db.data_service.services.models.ServiceDBEntity
import po.db.data_service.services.models.ServiceDataModel
import po.db.data_service.services.models.ServiceDataModelClass
import po.playground.projects.data_service.services.Partners


class PartnerEntity(id: EntityID<Long>) : ServiceDBEntity(id){
    companion object : LongEntityClass<PartnerEntity>(Partners)

    var name by Partners.name
    var legalName by Partners.legalName
    var regNr by Partners.regNr
    var vatNr by Partners.vatNr
    var created by Partners.created
    var updated by Partners.updated

    fun toDataModel():PartnerModel{
        val result = PartnerModel(
            0,  //this.id.value,
            this.name,
            this.legalName,
            this.regNr,
            this.vatNr
        ).also {
            it.created
            it.updated
        }
        return result
    }
}


@Serializable
data class PartnerModel(
    override var id: Long,
    var name : String,
    @SerialName("legal_name")
    var legalName: String,
    @SerialName("reg_nr")
    val regNr : String? = null,
    @SerialName("vat_nr")
    val vatNr : String? = null,
) : ServiceDataModel<PartnerEntity>() {
    companion object : ServiceDataModelClass<PartnerModel,PartnerEntity>(){

    }

    var created: LocalDateTime? = nowDateTime
    var updated: LocalDateTime? = nowDateTime

    override var sourceEntity: PartnerEntity? = null


    override fun updateEntity() = listOf(
        { sourceEntity?.name = updateString(this.name) },
        { sourceEntity?.legalName = updateString(this.legalName) },
        { sourceEntity?.regNr = updateStringOrNull(this.regNr) },
        { sourceEntity?.vatNr = updateStringOrNull(this.vatNr) }
    )


}