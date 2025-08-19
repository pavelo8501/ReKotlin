package po.exposify.dto.components.executioncontext

import po.exposify.dto.components.bindings.BindingHub
import po.exposify.dto.interfaces.DataModel

internal fun <D: DataModel> splitData (
    dataList: List<D>
):SplitLists<D>{
    return SplitLists(dataList)
}


//
//fun <DTO, D,  E, F, FD, FE>  runInsert(
//    context: DTOExecutionContext<DTO, D,  E, F, FD, FE>,
//    dataModels: List<FD>,
//    initialOperation: CrudOperation,
//): List<CommonDTO<F, FD, FE>> where DTO: ModelDTO, D: ModelDTO, E: LongEntity, F : ModelDTO, FD: DataModel, FE: LongEntity {
//    val savedDTOS = mutableListOf<CommonDTO<F, FD, FE>>()
//
//    dataModels.forEach { data ->
//
//        val commonDTO =   context.dtoFactory.createDto(data)
//        if (commonDTO.dataStatus == DataStatus.PreflightCheckMock) {
//            return savedDTOS
//        }
//        "runInsert spep 4 runInsert in context ${context.identifiedByName}".output(Colour.MAGENTA)
//
//        commonDTO.registerParentDTO(context.hostDTO)
//        val executionCTX = context
//        val persistedEntity = commonDTO.daoService.save { entity ->
//
//            commonDTO.bindingHub.updateEntity(entity)
//            // updateEntity(executionCTX, entity)
//            //commonDTO.bindingHub.resolveAttachedForeignUpdating(executionCTX, entity)
//            "Here it should be saving section entity and I need to provide page".output(Colour.MAGENTA)
//            "In reality wre doing for ${context.identifiedByName} anf as it ${context.identity}".output(Colour.MAGENTA)
//            val parentEntity = context.hostDTO.entityContainer.getValue(context)
//            val delegate = commonDTO.bindingHub.getParentDelegateByType(context.hostDTO.commonType)
//            TODO("Part of refactor")
////            delegate!!.bindEntity(
////                context,
////                entity,
////                parentEntity.castOrOperations(context.hostingDTO.commonType.entityType.kClass, context),
////            )
//        }
//        persistedEntity.flush()
//        savedDTOS.add(commonDTO)
//        commonDTO.entityContainer.provideValue(persistedEntity)
//        commonDTO.bindingHub.relationDelegateMap.values.forEach { childDelegate ->
//            TODO("Part of refactor")
////            val dataList  = childDelegate.extractDataCasting(context.childClass.commonDTOType.dataType.kClass, data)
////            commonDTO.withDTOContextCreating(childDelegate.dtoClass){childDto->
////                val childContext = this@withDTOContextCreating
////                context.insert(dataList)
////            }
//        }
//    }
//    return savedDTOS
//}

//fun <DTO, D,  E, F, FD, FE>   DTOExecutionContext<DTO, D,  E, F, FD, FE>.insert(
//
//    dataModels: List<FD>,
//): ResultList<F, FD>  where DTO: ModelDTO, D: ModelDTO, E: LongEntity, F : ModelDTO, FD: DataModel, FE: LongEntity{
//
//    return  withTransactionIfNone(dtoClass.debugger, warnIfNoTransaction = true) {
//        val operation = CrudOperation.Insert
//        val split = splitData(dataModels)
//
//        if (split.updateList.isNotEmpty()) {
//            operationsException(insertHasNon0IdMsg, methodMisuse)
//        }
//        val resultingList = runInsert(this,  split.insertList, operation)
//        //  listNotifier.trigger(ContextListEvents.InsertComplete, resultingList)
//        resultingList.toResult(dtoClass, operation)
//    }
//}