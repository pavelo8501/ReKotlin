package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.id.IdTable
import po.exposify.dto.CommonDTO
import po.exposify.dto.RootDTO
import po.exposify.dto.components.proFErty_binder.containerize
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ExecutionContext
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.getOrOperationsEx


class RootExecutionProvider<DTO, DATA, ENTITY>(
    val dtoClass: RootDTO<DTO, DATA, ENTITY>
): ExecutionContext<DTO, DATA, ENTITY> where  DTO  : ModelDTO , DATA : DataModel, ENTITY: LongEntity{

    override val providerName: String
        get() = dtoClass.qualifiedName


    private suspend fun createDto(entity: ENTITY):CommonDTO<DTO, DATA, ENTITY>{
        val dto = dtoClass.config.dtoFactory.createDto()
        dto.dtoPropertyBinder.update(entity.containerize(UpdateMode.ENTITY_TO_MODEL, null, true))
        dtoClass.registerDTO(dto)
        dto.getDtoRepositories().forEach { it.loadHierarchyByEntity() }
        return dto
    }

    private suspend fun insert(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY>{
        val dto = dtoClass.config.dtoFactory.createDto(dataModel)
        dtoClass.config.daoService.save(dto)
        dtoClass.registerDTO(dto)
        dto.getDtoRepositories().forEach { it.loadHierarchyByModel() }
        return ResultSingle(dto)
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

    override suspend fun pickById(id: Long): ResultSingle<DTO, DATA, ENTITY>{
        val existent = dtoClass.lookupDTO(id)
        return if(existent != null){
            ResultSingle(existent)
        }else{
            val entity = dtoClass.config.daoService.pickById(id).getOrOperationsEx("Entity with provided id :${id} not found")
            ResultSingle(createDto(entity))
        }
    }

    override suspend fun pick(conditions: Query): ResultSingle<DTO, DATA, ENTITY>{
        val entity = dtoClass.config.daoService.pick(conditions).getOrOperationsEx("Entity with provided query :${conditions} not found")
        val existent = dtoClass.lookupDTO(entity.id.value)
        return if(existent != null){
            ResultSingle(existent)
        }else{
            ResultSingle(createDto(entity))
        }
    }

    override suspend fun update(dataModel: DATA): ResultSingle<DTO, DATA, ENTITY>{
        if(dataModel.id == 0L){
            return insert(dataModel)
        }else{
           val existent =  dtoClass.lookupDTO(dataModel.id)
           if(existent != null){
               existent.dtoPropertyBinder.update(dataModel)
               existent.getDtoRepositories().forEach {repo->
                   repo.loadHierarchyByModel()
               }
               return ResultSingle(existent)
           }else{
              val entity = dtoClass.config.daoService.pickById(dataModel.id)
                   .getOrOperationsEx("Unable to update. DTO with id:${dataModel.id} not found.")
              val newDto = createDto(entity)
              dtoClass.registerDTO(newDto)
              newDto.dtoPropertyBinder.update(dataModel)
              return ResultSingle(newDto)
           }
        }
    }

    override suspend fun update(dataModels: List<DATA>): ResultList<DTO, DATA, ENTITY>{
        val result =  ResultList<DTO, DATA, ENTITY>()
        dataModels.forEach {
            result.appendDto(update(it))
        }
        return result
    }
}