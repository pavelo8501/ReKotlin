package po.exposify.dto.components.property_binder

import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.exceptions.OperationsException
import po.misc.types.getOrThrow



fun <ENTITY : ExposifyEntity, P_DTO: ModelDTO, PD: DataModel, PE: ExposifyEntity> ENTITY.containerize(
    updateMode : UpdateMode,
    parentDTO: CommonDTO<P_DTO, PD, PE>? = null
):EntityUpdateContainer<ENTITY, P_DTO, PD, PE> {
    val container = EntityUpdateContainer<ENTITY, P_DTO, PD, PE>(this, updateMode)
    parentDTO?.let {
        container.setParentData(parentDTO)
    }
    return container
}

data class EntityUpdateContainer<ENTITY : ExposifyEntity, P_DTO: ModelDTO, PD: DataModel, PE: ExposifyEntity>(
    val ownEntity: ENTITY,
    val updateMode : UpdateMode
){

    val isParentDtoSet : Boolean get() = parentDto!=null

    var parentDto: CommonDTO<P_DTO, PD, PE>? = null
    val hasParentDto : CommonDTO<P_DTO, PD, PE>
        get()= parentDto.getOrThrow<CommonDTO<P_DTO, PD, PE>, OperationsException>()

    fun extractParentEntity():PE?{
        return parentDto?.daoEntity
    }



    fun setParentData(dto: CommonDTO<P_DTO, PD, PE>):EntityUpdateContainer<ENTITY, P_DTO, PD, PE> {
        parentDto = dto
        return this
    }
}