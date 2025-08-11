package po.exposify.dto.components.bindings.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.DTOExecutionContext
import po.exposify.dto.enums.DataStatus
import po.exposify.dto.helpers.notification
import po.exposify.dto.helpers.warning
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.exceptions.operationsException
import po.exposify.extensions.castOrOperations

@PublishedApi
internal fun <DTO : ModelDTO, D : DataModel, E : LongEntity> RootDTO<DTO, D, E>.shallowDTO(): CommonDTO<DTO, D, E> {
    val emptyModel = dtoConfiguration.dtoFactory.createDataModel()
    val dto = dtoConfiguration.dtoFactory.createDto(emptyModel)

    dto.updateDataStatus(DataStatus.PreflightCheckMock)
    return dto
}

@Suppress("ktlint:standard:max-line-length")
internal fun <DTO : ModelDTO, D : DataModel, E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity> CommonDTO<DTO, D, E>.shallowDTO(
    dtoClass: DTOClass<F, FD, FE>,

): CommonDTO<F, FD, FE> {

    val parentDTO = this

    return withDTOContextCreating<DTO, D, E, F, FD, FE, CommonDTO<F, FD, FE>>(dtoClass) {
        val newDTO = dtoFactory.createDto()
        newDTO.registerParentDTO(parentDTO)
        newDTO.updateDataStatus(DataStatus.PreflightCheckMock)
        newDTO
    }
}

internal fun <DTO : ModelDTO, D : DataModel, E : LongEntity> DTOBase<DTO, D, E>.newDTO(): CommonDTO<DTO, D, E> {
    val emptyDataModel = dtoConfiguration.dtoFactory.createDataModel()
    val dto = dtoConfiguration.dtoFactory.createDto(emptyDataModel)
    return dto
}

internal fun <DTO : ModelDTO, D : DataModel, E : LongEntity> DTOBase<DTO, D, E>.newDTO(dataModel:D): CommonDTO<DTO, D, E> {
    val dto = dtoConfiguration.dtoFactory.createDto(dataModel)
    return dto
}


internal fun <F, FD, FE, DTO, D, E> CommonDTO<DTO, D, E>.createDTOContext(
    dtoClass: DTOClass<F, FD, FE>
): DTOExecutionContext<F, FD, FE, DTO, D, E>
        where DTO : ModelDTO, D : DataModel, E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity {
    val newContext = DTOExecutionContext(dtoClass, this)
    registerExecutionContext(dtoClass.commonDTOType, newContext)
    return newContext
}

internal inline fun <DTO, D, F, FD, R> CommonDTO<DTO, D, *>.withDTOContext(
    commonDTOType: CommonDTOType<F, FD, *>,
    throwIfNull: Boolean = false,
    block: DTOExecutionContext<F, FD, *, DTO, D, *>.() -> R,
): R? where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel, R : Any {

    val context = executionContextMap[commonDTOType]
    if(context == null && throwIfNull){
       throw operationsException("DTOContext for key $commonDTOType not found", ExceptionCode.BAD_DTO_SETUP)
    }

    if(context != null){
        val casted = context.castOrOperations<DTOExecutionContext<F, FD, *, DTO, D, *>>(this)
        return block.invoke(casted)
    }else{
        warning("DTOContext for key $commonDTOType not found")
        return null
    }
}

inline fun <DTO, D, E, F, FD, FE, R> withDTOContext(
    context :  DTOExecutionContext<F, FD, E, DTO, D, FE,>,
    block: DTOExecutionContext<F, FD, E, DTO, D, FE>.() -> R,
): R where DTO : ModelDTO, D : DataModel, E: LongEntity, F : ModelDTO, FD : DataModel, FE:LongEntity, R : Any {

    return block.invoke(context)
}


internal inline fun <DTO, D, E, F, FD, FE, R>  CommonDTO<DTO, D, E>.withDTOContextCreating(
    dtoClass:DTOClass<F, FD, FE>,
    block: DTOExecutionContext<F, FD, FE, DTO, D, E>.() -> R,
): R where DTO : ModelDTO, D : DataModel, E: LongEntity, F : ModelDTO, FD : DataModel, FE:LongEntity, R : Any {
   val context = executionContextMap[dtoClass.commonDTOType]
   return if(context != null){
        val casted = context.castOrOperations<DTOExecutionContext<F, FD, FE, DTO, D, E>>(this)
        notify("Reusing context from ExecutionContextMap of $this")
        block.invoke(casted)
    }else{
       val newContext = this.createDTOContext(dtoClass)
        executionContextMap[dtoClass.commonDTOType] = newContext
        notify("Created new  ExecutionContext for $dtoClass")
        block.invoke(newContext)
    }
}

@JvmName("withDTOContextCreatingNoEntity")
internal inline fun <DTO, D, F, FD, R>  withDTOContextCreating(
    hostingDTO: CommonDTO<DTO, D, *>,
    dtoClass:DTOClass<F, FD, *>,
    block: DTOExecutionContext<F, FD, *, DTO, D, *>.() -> R,
): R where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel, R : Any {

    val context = hostingDTO.executionContextMap[dtoClass.commonDTOType]
    return if(context != null){
        val casted = context.castOrOperations<DTOExecutionContext<F, FD, *, DTO, D, *>>(hostingDTO)
        hostingDTO.notification("Reusing context from ExecutionContextMap of $hostingDTO")
        block.invoke(casted)
    }else{
        val newContext = hostingDTO.createDTOContext(dtoClass)
        hostingDTO.executionContextMap[dtoClass.commonDTOType] = newContext
        hostingDTO.notification("Created new  ExecutionContext for $dtoClass")
        block.invoke(newContext)
    }
}

