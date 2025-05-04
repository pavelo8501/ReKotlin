package po.exposify.dto.components.proFErty_binder

import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.exposify.exceptions.OperationsException
import po.misc.types.getOrThrow


fun <TE : ExposifyEntity, F_DTO: ModelDTO, FD: DataModel, FE: ExposifyEntity> TE.containerize(
    updateMode : UpdateMode,
    parentDTO: CommonDTO<F_DTO, FD, FE>? = null
):EntityUpdateContainer<TE, F_DTO, FD, FE> {
    val container = EntityUpdateContainer<TE, F_DTO, FD, FE>(this, updateMode)
    parentDTO?.let {
        container.setParentData(parentDTO)
    }
    return container
}

data class EntityUpdateContainer<TE : ExposifyEntity, F_DTO: ModelDTO, FD: DataModel, FE: ExposifyEntity>(
    val ownEntity: TE,
    val updateMode : UpdateMode
){

    val isParentDtoSet : Boolean get() = parentDto!=null

    var parentDto: CommonDTO<F_DTO, FD, FE>? = null
    val hasParentDto : CommonDTO<F_DTO, FD, FE>
        get()= parentDto.getOrThrow<CommonDTO<F_DTO, FD, FE>, OperationsException>()

    fun extractParentEntity():FE?{
        return parentDto?.daoEntity
    }

    fun setParentData(dto: CommonDTO<F_DTO, FD, FE>):EntityUpdateContainer<TE, F_DTO, FD, FE> {
        parentDto = dto
        return this
    }
}