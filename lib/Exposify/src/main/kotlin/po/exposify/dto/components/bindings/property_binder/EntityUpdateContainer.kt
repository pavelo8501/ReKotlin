package po.exposify.dto.components.proFErty_binder

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.bindings.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.exceptions.OperationsException
import po.misc.types.getOrThrow


fun <E : LongEntity, F_DTO: ModelDTO, FD: DataModel, FE: LongEntity> E.containerize(
    updateMode : UpdateMode,
    foreignDTO: CommonDTO<F_DTO, FD, FE>? = null,
    inserted: Boolean = false
):EntityUpdateContainer<E, F_DTO, FD, FE> {
    val container = EntityUpdateContainer<E, F_DTO, FD, FE>(this, updateMode, inserted)
    foreignDTO?.let {
        container.setParentData(foreignDTO)
    }
    return container
}

data class EntityUpdateContainer<TE : LongEntity, F_DTO: ModelDTO, FD: DataModel, FE: LongEntity>(
    val ownEntity: TE,
    val updateMode : UpdateMode,
    private var isEntityInserted: Boolean = false
){

    val isParentDtoSet : Boolean get() = parentDto!=null
    var parentDto: CommonDTO<F_DTO, FD, FE>? = null
    val hasParentDto : CommonDTO<F_DTO, FD, FE>
        get()= parentDto.getOrThrow<CommonDTO<F_DTO, FD, FE>, OperationsException>()


    fun insertedEntity(value : Boolean): EntityUpdateContainer<TE, F_DTO, FD, FE>{
        isEntityInserted = value
        return this
    }

    val inserted: Boolean get() = isEntityInserted

    fun extractParentEntity():FE?{
        return parentDto?.entity
    }

    fun setParentData(dto: CommonDTO<F_DTO, FD, FE>):EntityUpdateContainer<TE, F_DTO, FD, FE> {
        parentDto = dto
        return this
    }
}