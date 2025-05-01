package po.exposify.dto.components.property_binder

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.exceptions.OperationsException
import po.misc.types.getOrThrow



fun <ENTITY : ExposifyEntity, P_DTO: ModelDTO, PD: DataModel, PE: ExposifyEntity> ENTITY.containerize()
    :EntityUpdateContainer<ENTITY, P_DTO, PD, PE> {

    return EntityUpdateContainer(this)
}

data class EntityUpdateContainer<ENTITY : ExposifyEntity, P_DTO: ModelDTO, PD: DataModel, PE: ExposifyEntity>(
    val ownEntity: ENTITY
){

    var parentDataSet : Boolean = false
        private set

    private var parentEntity:PE? = null
    val hasParentEntity : PE get()= parentEntity.getOrThrow<PE, OperationsException>()

    var parentDto: CommonDTO<P_DTO, PD, PE>? = null
    val hasParentDto : CommonDTO<P_DTO, PD, PE>
        get()= parentDto.getOrThrow<CommonDTO<P_DTO, PD, PE>, OperationsException>()


    fun setParentData(entity:PE, dto: CommonDTO<P_DTO, PD, PE>)
    :EntityUpdateContainer<ENTITY, P_DTO, PD, PE>
    {
        parentEntity = entity
        parentDto = dto
        parentDataSet = true
        return this
    }
}