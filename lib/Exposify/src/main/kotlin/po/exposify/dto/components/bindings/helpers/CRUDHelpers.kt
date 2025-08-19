package po.exposify.dto.components.bindings.helpers

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.annotations.ExecutionContextLauncher
import po.exposify.dto.components.executioncontext.DTOExecutionContext
import po.exposify.dto.enums.DataStatus
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.misc.types.safeCast


internal fun <DTO, D, E, F, FD, FE> CommonDTO<DTO, D, E>.shallowDTO(
    dtoClass: DTOClass<F, FD, FE>,
): CommonDTO<F, FD, FE>
where DTO : ModelDTO, D : DataModel, E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity {

    val parentDTO = this
    return withDTOContextCreating(dtoClass){
        val newDTO = dtoClass.newDTO(DataStatus.PreflightCheckMock)
        newDTO.bindingHub.resolveParent(parentDTO)
        newDTO
    }
}

internal fun <DTO : ModelDTO, D : DataModel, E : LongEntity> DTOBase<DTO, D, E>.newDTO(
    dataStatus:DataStatus = DataStatus.New
): CommonDTO<DTO, D, E> {
    val emptyDataModel = dtoConfiguration.dtoFactory.createDataModel()
    val dto = dtoConfiguration.dtoFactory.createDto(emptyDataModel)
    dto.updateDataStatus(dataStatus)
    return dto
}


internal fun <DTO : ModelDTO, D : DataModel, E : LongEntity> DTOBase<DTO, D, E>.newDTO(dataModel:D): CommonDTO<DTO, D, E> {
    val dto = dtoConfiguration.dtoFactory.createDto(dataModel)
    return dto
}

@PublishedApi
internal fun <DTO : ModelDTO, D : DataModel, E : LongEntity> RootDTO<DTO, D, E>.shallowDTO(

): CommonDTO<DTO, D, E> = newDTO(DataStatus.PreflightCheckMock)




//internal fun <DTO, D, E, F, FD, FE> CommonDTO<DTO, D, E>.createContext(
//    dtoClass: DTOClass<F, FD, FE>
//): DTOExecutionContext<DTO, D, E, F, FD, FE>
//        where DTO : ModelDTO, D : DataModel, E : LongEntity, F : ModelDTO, FD : DataModel, FE : LongEntity
//{
//            this.createExecutionContext()
//    val newContext = DTOExecutionContext(dtoClass, this)
//    registerExecutionContext(dtoClass.commonDTOType, newContext)
//    return newContext
//}

//internal inline fun <DTO, D, F, FD, R> CommonDTO<DTO, D, *>.withDTOContext(
//    commonDTOType: CommonDTOType<F, FD, *>,
//    throwIfNull: Boolean = false,
//    block: DTOExecutionContext<F, FD, *, DTO, D, *>.() -> R,
//): R? where DTO : ModelDTO, D : DataModel, F : ModelDTO, FD : DataModel, R : Any {
//
//    val context = executionContextMap[commonDTOType]
//    if(context == null && throwIfNull){
//       throw operationsException("DTOContext for key $commonDTOType not found", ExceptionCode.BAD_DTO_SETUP)
//    }
//
//    if(context != null){
//        val casted = context.castOrOperations<DTOExecutionContext<F, FD, *, DTO, D, *>>(this)
//        return block.invoke(casted)
//    }else{
//        warning("DTOContext for key $commonDTOType not found")
//        return null
//    }
//}


@ExecutionContextLauncher()
inline fun <DTO, D, E, F, FD, FE, R> withDTOContext(
    context :  DTOExecutionContext<F, FD, E, DTO, D, FE,>,
    block: DTOExecutionContext<F, FD, E, DTO, D, FE>.() -> R,
): R where DTO : ModelDTO, D : DataModel, E: LongEntity, F : ModelDTO, FD : DataModel, FE:LongEntity, R : Any {

    return block.invoke(context)
}

@ExecutionContextLauncher()
internal inline fun <DTO, D, E, F, FD, FE, R>  CommonDTO<DTO, D, E>.withDTOContextCreating(
    dtoClass:DTOClass<F, FD, FE>,
    block: DTOExecutionContext<DTO, D, E, F, FD, FE>.(CommonDTO<DTO, D, E>) -> R,
): R where DTO : ModelDTO, D : DataModel, E: LongEntity, F : ModelDTO, FD : DataModel, FE:LongEntity, R : Any {

   val context =  executionContextMap[dtoClass]?.safeCast<DTOExecutionContext<DTO, D, E, F, FD, FE>>()
   return context?.let {inContext->
        notify("Reusing existent context:  ${inContext.identifiedByName}")
        block.invoke(inContext, this)
    }?:run {
        notify("Creating new DTOContext for $identifiedByName")
        val newContext =  createExecutionContext(dtoClass)
        executionContextMap[dtoClass] = newContext
        block.invoke(newContext, this)
    }
}
