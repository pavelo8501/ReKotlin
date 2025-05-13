package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.extensions.getOrOperationsEx

class ResultList<DTO, DATA, ENTITY>  (
   private val initialList : List<CommonDTO<DTO, DATA, ENTITY>>? = null
)  where DTO : ModelDTO, DATA: DataModel, ENTITY : LongEntity {

    internal val dtoList: MutableList<CommonDTO<DTO, DATA, ENTITY>> = mutableListOf()

    init {
        initialList?.let {
            dtoList.addAll(it)
        }
    }


    fun addList(list: List<CommonDTO<DTO, DATA, ENTITY>>): ResultList<DTO, DATA, ENTITY> {
        dtoList.addAll(list)
        return this
    }

    internal fun appendDto(dto: CommonDTO<DTO, DATA, ENTITY>): ResultList<DTO, DATA, ENTITY> {
        dtoList.add(dto)
        return this
    }

    internal fun appendDto(single: ResultSingle<DTO, DATA, ENTITY>): ResultList<DTO, DATA, ENTITY> {
        single.getAsCommonDTO()?.let {
            dtoList.add(it)
        }
        return this
    }

    fun getData(): List<DATA> {
        val dataModels = dtoList.map { it.dataModel }
        return dataModels
    }

    fun getDTO(): List<CommonDTO<DTO, DATA, ENTITY>> {
        return dtoList
    }
}

class ResultSingle<DTO, DATA, ENTITY>(
    private var rootDTO: CommonDTO<DTO, DATA, ENTITY>? = null
) where DTO : ModelDTO, DATA: DataModel, ENTITY : LongEntity {


    internal fun appendDto(dto: CommonDTO<DTO, DATA, ENTITY>): ResultSingle<DTO, DATA, ENTITY> {
        rootDTO = dto
        return this
    }

    fun getData(): DATA? {
        val dataModel =  rootDTO?.dataModel
        return dataModel
    }

    fun getDataForced(): DATA {
        val dataModel = getDTOForced().dataModel
        return dataModel
    }

    internal fun getAsCommonDTO(): CommonDTO<DTO, DATA, ENTITY>? {
        return rootDTO
    }

    fun getDTO(): DTO? {
        @Suppress("UNCHECKED_CAST")
        return rootDTO as? DTO
    }

    fun getDTOForced(): CommonDTO<DTO, DATA, ENTITY> {
        return rootDTO.getOrOperationsEx("No result")
    }


}