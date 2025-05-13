package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.CommonDTO
import po.exposify.dto.DTOClass
import po.exposify.dto.RootDTO
import po.exposify.dto.components.proFErty_binder.containerize
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CrudOperation
import po.exposify.dto.models.trackSave
import po.exposify.extensions.castOrOperationsEx
import po.exposify.extensions.getOrOperationsEx


class RootExecutionProvider<DTO, DATA, ENTITY>(
    val dtoClass: RootDTO<DTO, DATA, ENTITY>
): ExecutionContext<DTO, DATA, ENTITY> where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity{

    override val providerName: String
        get() = dtoClass.qualifiedName


    private suspend fun createDto(
        entity: ENTITY
    ):CommonDTO<DTO, DATA, ENTITY>{
        val dto = dtoClass.config.dtoFactory.createDto()
        dto.dtoPropertyBinder.update(entity.containerize(UpdateMode.ENTITY_TO_MODEL, null, true))
        dtoClass.registerDTO(dto)
        dto.getDtoRepositories().forEach { it.loadHierarchyByEntity() }
        return dto
    }

    private suspend fun createDto(
        dataModel: DATA
    ):CommonDTO<DTO, DATA, ENTITY>
    {
        val dto = dtoClass.config.dtoFactory.createDto(dataModel)
        if(dataModel.id == 0L){
            dto.trackSave(CrudOperation.Save, dto.daoService).let {
                dto.daoService.save(dto)
                dto.repositories.forEach {
                    it.value.update(dataModel)
                }
                it.addTrackInfoResult(1)
            }
        }else{
            dto.trackSave(CrudOperation.Update, dto.daoService).let {
                dto.daoService.update(dto.castOrOperationsEx("updateDto(update). Cast failed."))
                it.addTrackInfoResult(1)
            }
        }
        dto.getDtoRepositories().forEach {repository->
            repository.loadHierarchyByModel()
        }
        return dto
    }

    override suspend fun select(): ResultList<DTO, DATA, ENTITY> {
        val result = ResultList<DTO, DATA, ENTITY>()
        val entities =    dtoClass.config.daoService.select()
        entities.forEach {
            val newDto =  createDto(it)
            result.appendDto(newDto)
        }
        return result
    }
    override suspend fun select(conditions: Query): ResultList<DTO, DATA, ENTITY> {
        val result = ResultList<DTO, DATA, ENTITY>()
        val entities =  dtoClass.config.daoService.select(conditions)
        entities.forEach {
            val newDto =  createDto(it)
            result.appendDto(newDto)
        }
        return result
    }
    override suspend fun <T : IdTable<Long>> select(conditions: WhereQuery<T>): ResultList<DTO, DATA, ENTITY>
            = select(conditions)

    override suspend fun pickById(id: Long): ResultSingle<DTO, DATA, ENTITY> {

        val entity = dtoClass.config.daoService.pickById(id).getOrOperationsEx("Entity with provided id :${id} not found")
        val dto =  createDto(entity)
        return ResultSingle(dto)
    }
    override suspend fun pick(conditions: Query): ResultSingle<DTO, DATA, ENTITY> {

        val entity = dtoClass.config.daoService.pick(conditions).getOrOperationsEx("Entity with provided query :${conditions} not found")
        val dto =  createDto(entity)
        return ResultSingle(dto)
    }

    override suspend fun update(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY> {
        val dto = createDto(dataModel)
        return ResultSingle(dto)
    }

    override suspend fun update(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY> {
        val result =  ResultList<DTO, DATA, ENTITY>()
        dataModels.forEach {
            result.appendDto(update(it))
        }
        return result
    }
}

//
//class ClassExecutionProvider<F_DTO, FD, FE, DTO, DATA, ENTITY>(
//    val dto : CommonDTO<F_DTO, FD, FE>,
//    val childClass: DTOClass<DTO, DATA, ENTITY>,
//):ExecutionContext<DTO, DATA, ENTITY>
//        where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity,
//               F_DTO: ModelDTO, FD : DataModel, FE: LongEntity
//{
//
//    override val providerName: String
//        get() = childClass.qualifiedName
//
//    override suspend fun select(): ResultList<DTO, DATA, ENTITY> {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun <T : IdTable<Long>> select(conditions: WhereQuery<T>): ResultList<DTO, DATA, ENTITY> {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun select(conditions: Query): ResultList<DTO, DATA, ENTITY> {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun pickById(id: Long): ResultSingle<DTO, DATA, ENTITY> {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun pick(conditions: Query): ResultSingle<DTO, DATA, ENTITY> {
//        TODO("Not yet implemented")
//    }
//
//    override suspend fun update(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY> {
//        val result = ResultList<DTO, DATA, ENTITY>()
//        dataModels.forEach {
//            result.appendDto(update(it))
//        }
//        return result
//    }
//
//    private suspend fun insert(dataModel: DATA): CommonDTO<DTO, DATA, ENTITY>{
//        val dto = childClass.config.dtoFactory.createDto()
//        dto.trackSave(CrudOperation.Save, dto.daoService).let {
//            dto.daoService.save(dto)
//            dto.repositories.forEach {
//                it.value.update(dataModel)
//            }
//            it.addTrackInfoResult(1)
//        }
//        return dto
//    }
//
//    override suspend fun update(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY> {
//        val existent = childClass.lookupDTO(dataModel.id)
//        if (existent != null) {
//            existent.dtoPropertyBinder.update(dataModel)
//            return ResultSingle(existent)
//        } else {
//            val result = ResultSingle<DTO, DATA, ENTITY>()
//
//            if(dataModel.id == 0L){
//                result.appendDto(insert(dataModel))
//            }else{
//
//                dto.getDtoRepositories(childClass).forEach { repo ->
//                    val updatedDto = repo.update(dataModel)
//                    result.appendDto(updatedDto)
//                }
//            }
//            return result
//        }
//    }
//}