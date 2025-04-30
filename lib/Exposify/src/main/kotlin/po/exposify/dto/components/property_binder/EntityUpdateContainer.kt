package po.exposify.dto.components.property_binder

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity
import po.misc.types.getOrThrow



fun <ENTITY : ExposifyEntity, PARENT_ENTITY: ExposifyEntity> ENTITY.containerize()
    :EntityUpdateContainer<ENTITY, PARENT_ENTITY> {

    return EntityUpdateContainer(this)
}

data class EntityUpdateContainer<ENTITY : ExposifyEntity, PARENT_ENTITY: ExposifyEntity>(
    val ownEntity: ENTITY
){

    var parentDataSet : Boolean = false
        private set

    private var parentEntity:PARENT_ENTITY? = null
    val hasParentEntity : PARENT_ENTITY get()= parentEntity.getOrThrow<PARENT_ENTITY, po.exposify.exceptions.OperationsException>()

    var parentDto: CommonDTO<ModelDTO, DataModel, PARENT_ENTITY>? = null
    val hasParentDto : CommonDTO<ModelDTO, DataModel, PARENT_ENTITY>
        get()= parentDto.getOrThrow<CommonDTO<ModelDTO, DataModel, PARENT_ENTITY>, po.exposify.exceptions.OperationsException>()


    fun setParentData(entity:PARENT_ENTITY, dto: CommonDTO<ModelDTO, DataModel, PARENT_ENTITY>)
    :EntityUpdateContainer<ENTITY, PARENT_ENTITY>
    {
        parentEntity = entity
        parentDto = dto
        parentDataSet = true
        return this
    }
}