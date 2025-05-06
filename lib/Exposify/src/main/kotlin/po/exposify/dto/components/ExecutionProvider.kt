package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ChildClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.ModelDTO


class ExecutionProvider<DTO, DATA, ENTITY>(
    override val hostingDTO: CommonDTO<DTO, DATA, ENTITY>
): ExecutionContext<DTO, DATA, ENTITY> where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity{


    override suspend fun <C_DTO : ModelDTO, CD : DataModel> select(
        childClass: ChildClass<C_DTO, CD>
    ): ResultList<C_DTO, CD> {
        val repository = hostingDTO.getOneToManyRepository<C_DTO, CD, LongEntity>(childClass)
        return ResultList(repository.getDTO())
    }

    override suspend fun <C_DTO : ModelDTO, CD : DataModel> pickById(
        childClass: ChildClass<C_DTO, CD>,
        id: Long
    ): ResultSingle<C_DTO, CD> {

      val repository = hostingDTO.getOneToOneRepository<C_DTO, CD, LongEntity>(childClass)
      return  ResultSingle(repository.pickById(id))
    }

//
//    internal suspend fun selectDto(
//        entity: ENTITY
//    ):CommonDTO<DTO, DATA, ENTITY>{
//        val dto = hostingDTO.dtoClassConfig.dtoFactory.createDto()
//        dto.updatePropertyBinding(entity, UpdateMode.ENTITY_TO_MODEL, entity.containerize(UpdateMode.ENTITY_TO_MODEL))
//        dto.getDtoRepositories().forEach { it.loadHierarchy() }
//        return dto.castOrOperationsEx("selectDto. Cast failed.")
//    }
//
//    internal suspend fun updateDto(
//        dataModel: DATA
//    ):CommonDTO<DTO, DATA, ENTITY>
//    {
//        val dto = hostingDTO.dtoClassConfig.dtoFactory.createDto(dataModel)
//        if(dataModel.id == 0L){
//            dto.trackSave(CrudOperation.Save, dto.daoService).let {
//                dto.daoService.save(dto.castOrOperationsEx("updateDto(save). Cast failed."))
//                it.addTrackInfoResult(1)
//            }
//        }else{
//            dto.trackSave(CrudOperation.Update, dto.daoService).let {
//                dto.daoService.update(dto.castOrOperationsEx("updateDto(update). Cast failed."))
//                it.addTrackInfoResult(1)
//            }
//        }
//        dto.getDtoRepositories().forEach {repository->
//            repository.update()
//        }
//        return dto.castOrOperationsEx("updateDto(Return). Cast failed.")
//    }
//
//
//    internal suspend inline fun pickById(
//        id: Long
//    ): ResultSingle<DTO, DATA> {
//
//        val entity =  hostingDTO.dtoClassConfig.daoService.pickById(id)
//        val checkedEntity = entity.getOrOperationsEx("Entity not found for id $id", ExceptionCode.VALUE_NOT_FOUND)
//        val dto = selectDto(hostingDTO.dtoClass, checkedEntity)
//        return ResultSingle(dto)
//    }


//    internal suspend inline fun <TB : IdTable<Long>> pick(
//        conditions: Query<TB>
//    ): ResultSingle<DTO, DATA> {
//        val entity = dtoClass.config.daoService.pick(conditions)
//        val checkedEntity = entity.getOrOperationsEx("Entity not found for conditions ${conditions.toString()}", ExceptionCode.VALUE_NOT_FOUND)
//        val dto = selectDto(dtoClass, checkedEntity)
//        return ResultSingle(dto)
//    }
//
//
//    internal suspend fun <T> select(
//        conditions:  Query<T>
//    ): ResultList<DTO, DATA> where  T: IdTable<Long> =
//        subTask("Select with conditions"){handler->
//
//            isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
//                true
//            }
//            val entities = dtoClass.config.daoService.select<T>(conditions)
//            val result =  ResultList<DTO, DATA>()
//            entities.forEach {
//                val newDto = selectDto(dtoClass, it)
//                result.appendDto(newDto)
//            }
//            handler.info("Created count ${result.rootDTOs.count()} DTOs")
//            result
//        }.resultOrException()
//
//
//    internal suspend fun select(): ResultList<DTO, DATA>
//            = subTask("Select") {handler->
//        isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
//            true
//        }
//        val entities = dtoClass.config.daoService.select()
//        val result =  ResultList<DTO, DATA>()
//        entities.forEach {
//            val newDto = selectDto(dtoClass, it)
//            result.appendDto(newDto)
//        }
//        handler.info("Created count ${result.rootDTOs.count()} DTOs ")
//        result
//    }.resultOrException()
//
//    internal suspend fun update(
//        dataModel: DATA,
//    ): ResultSingle<DTO, DATA>
//            = subTask("Update Repository.kt")  { handler->
//        isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
//            true
//        }
//        val dto = updateDto<DTO, DATA, ExposifyEntity>(dtoClass, dataModel)
//        handler.info("Created single DTO ${dto.dtoName}")
//        ResultSingle(dto)
//    }.resultOrException()
//
//    internal suspend fun update(
//        dataModels: List<DATA>,
//    ): ResultList<DTO, DATA>
//            = subTask("Update Repository.kt")  { handler->
//        isTransactionReady().testOrThrow(OperationsException("Transaction Lost Context", ExceptionCode.DB_NO_TRANSACTION_IN_CONTEXT)){
//            true
//        }
//        val result =  ResultList<DTO,DATA>()
//        dataModels.forEach {
//            val dto = updateDto<DTO, DATA, ExposifyEntity>(dtoClass, it)
//            result.appendDto(dto)
//        }
//        handler.info("Created DTOs ${result.rootDTOs.count()}")
//        result
//    }.resultOrException()
//
//
//    private suspend inline fun delete(
//        dataModel: DATA
//    ): ResultList<DTO, DATA>?
//    {
//        return null
//    }
//


}