package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOBase
import po.exposify.dto.RootDTO
import po.exposify.dto.components.proFErty_binder.containerize
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ChildClass
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ExecutionContext2
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CrudOperation
import po.exposify.dto.models.trackSave
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx


class RootExecutionProvider<DTO, DATA, ENTITY>(
    val dtoClass: RootDTO<DTO, DATA>
): ExecutionContext2<DTO, DATA, ENTITY> where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity{

    override val providerName: String
        get() = dtoClass.qualifiedName

    data class EntityPack<ENTITY>(val entity: LongEntity){

        fun getTypedEntity(): ENTITY{
            @Suppress("UNCHECKED_CAST")
            return entity as ENTITY
        }
    }

    private suspend fun createDto(
        entity: ENTITY
    ):CommonDTO<DTO, DATA, LongEntity>{
        val dto = dtoClass.config.dtoFactory.createDto()
        dto.updatePropertyBinding(entity, UpdateMode.ENTITY_TO_MODEL, entity.containerize(UpdateMode.ENTITY_TO_MODEL))
        dto.getDtoRepositories().forEach { it.loadHierarchy() }
        return dto.castOrOperationsEx("selectDto. Cast failed.")
    }

    private suspend fun <DTO : ModelDTO, DATA: DataModel, ENTITY: LongEntity> createDto(
        dataModel: DATA
    ):CommonDTO<DTO, DATA, ENTITY>
    {
        val dto = dtoClass.config.dtoFactory.createDto(dataModel)
        if(dataModel.id == 0L){
            dto.trackSave(CrudOperation.Save, dto.daoService).let {
                dto.daoService.save(dto.castOrOperationsEx("updateDto(save). Cast failed."))
                it.addTrackInfoResult(1)
            }
        }else{
            dto.trackSave(CrudOperation.Update, dto.daoService).let {
                dto.daoService.update(dto.castOrOperationsEx("updateDto(update). Cast failed."))
                it.addTrackInfoResult(1)
            }
        }
        dto.getDtoRepositories().forEach {repository->
            repository.update()
        }
        return dto.castOrOperationsEx("updateDto(Return). Cast failed.")
    }

    override suspend fun select(): ResultList<DTO, DATA> {
        val result = ResultList<DTO, DATA>()
        val entities =    dtoClass.config.daoService.select()
        entities.forEach {
            EntityPack<ENTITY>(it).castOrOperationsEx<EntityPack<ENTITY>>()
            val newDto =  createDto(EntityPack<ENTITY>(it).getTypedEntity())
            result.appendDto(newDto)
        }
        return result
    }
    override suspend fun select(conditions: Query): ResultList<DTO, DATA> {
        val result = ResultList<DTO, DATA>()
        val entities =  dtoClass.config.daoService.select(conditions)
        entities.forEach {
            EntityPack<ENTITY>(it).castOrOperationsEx<EntityPack<ENTITY>>()
            val newDto =  createDto(EntityPack<ENTITY>(it).getTypedEntity())
            result.appendDto(newDto)
        }
        return result
    }
    override suspend fun <T : IdTable<Long>> select(conditions: WhereQuery<T>): ResultList<DTO, DATA>
            = select(conditions)

    override suspend fun pickById(id: Long): ResultSingle<DTO, DATA> {
        val entity = dtoClass.config.daoService.pickById(id).getOrOperationsEx("Entity with provided id :${id} not found")
        val dto =  createDto(EntityPack<ENTITY>(entity).getTypedEntity())
        return ResultSingle(dto)
    }
    override suspend fun pick(conditions: Query): ResultSingle<DTO, DATA> {
        val entity = dtoClass.config.daoService.pick(conditions).getOrOperationsEx("Entity with provided query :${conditions} not found")
        val dto =  createDto(EntityPack<ENTITY>(entity).getTypedEntity())
        return ResultSingle(dto)
    }

    override suspend fun update(dataModels: List<DATA>): ResultList<DTO, DATA> {
        val result =  ResultList<DTO, DATA>()
        dataModels.forEach {
            val dto = createDto<DTO, DATA, LongEntity>(it)
            result.appendDto(dto)
        }
        return result
    }
}


class ClassExecutionProvider<DTO, DATA, ENTITY>(
    val childClass: ChildClass<DTO, DATA>
):ExecutionContext2<DTO, DATA, ENTITY>
        where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity
{

    override val providerName: String
        get() = childClass.qualifiedName

    override suspend fun select(): ResultList<DTO, DATA> {
        TODO("Not yet implemented")
    }

    override suspend fun <T : IdTable<Long>> select(conditions: WhereQuery<T>): ResultList<DTO, DATA> {
        TODO("Not yet implemented")
    }

    override suspend fun select(conditions: Query): ResultList<DTO, DATA> {
        TODO("Not yet implemented")
    }

    override suspend fun pickById(id: Long): ResultSingle<DTO, DATA> {
        TODO("Not yet implemented")
    }

    override suspend fun pick(conditions: Query): ResultSingle<DTO, DATA> {
        TODO("Not yet implemented")
    }

    override suspend fun update(dataModels: List<DATA>): ResultList<DTO, DATA> {
        TODO("Not yet implemented")
    }

//
//    override suspend fun <C_DTO : ModelDTO, CD : DataModel> pickById(
//        childClass: ChildClass<C_DTO, CD>,
//        id: Long
//    ): ResultSingle<C_DTO, CD> {
//
//        val repository = hostingDTO.getOneToOneRepository<C_DTO, CD, LongEntity>(childClass)
//        return ResultSingle(repository.pickById(id))
//    }
//
//    override suspend fun <C_DTO : ModelDTO, CD : DataModel, T: IdTable<Long>> pick(
//        childClass: ChildClass<C_DTO, CD>,
//        conditions: WhereQuery<T>
//    ): ResultSingle<C_DTO, CD> {
//
//        val repository = hostingDTO.getOneToOneRepository<C_DTO, CD, LongEntity>(childClass)
//        return ResultSingle(repository.pick(conditions))
//    }
//
//    override suspend fun <C_DTO : ModelDTO, CD : DataModel> select(
//        childClass: ChildClass<C_DTO, CD>
//    ): ResultList<C_DTO, CD> {
//        val repository = hostingDTO.getOneToManyRepository<C_DTO, CD, LongEntity>(childClass)
//        return ResultList(repository.getDTO())
//    }
//
//    suspend fun <T : IdTable<Long>> select(
//        childClass: DTOClass<*,*>,
//        conditions: WhereQuery<T>
//    ): ResultList<DTO, DATA> {
//
//        val repository = hostingDTO.getOneToManyRepository(childClass)
//        val result = repository.select(conditions)
//        return ResultList(repository.select(conditions))
//    }

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