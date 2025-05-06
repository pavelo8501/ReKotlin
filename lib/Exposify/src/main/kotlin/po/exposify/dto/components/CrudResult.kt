package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO

class ResultList<DTO, DATA>  (
   private val initialList : List<CommonDTO<DTO, DATA, LongEntity>>? = null
)  where DTO : ModelDTO, DATA: DataModel{

    internal val dtoList: MutableList<CommonDTO<DTO, DATA, LongEntity>> = mutableListOf()

    init {
        initialList?.let {
            dtoList.addAll(it)
        }
    }


    fun addList(list: List<CommonDTO<DTO, DATA, LongEntity>>):ResultList<DTO, DATA>{
        dtoList.addAll(list)
        return  this
    }

    internal fun appendDto(dto:CommonDTO<DTO, DATA, LongEntity>):ResultList<DTO, DATA>{
        dtoList.add(dto)
        return  this
    }

    fun getData(): List<DATA> {
        val dataModels =  dtoList.map { it.dataModel }
        return dataModels
    }

    fun getDTO(): List<CommonDTO<DTO, DATA, LongEntity>> {
        return dtoList
    }

    internal fun fromListResult(result: ResultList<DTO, DATA>): ResultList<DTO, DATA>{
        dtoList.clear()
        dtoList.addAll(result.dtoList)
        return this
    }

   internal fun fromSingleResult(result: ResultSingle<DTO, DATA>):ResultSingle<DTO, DATA> {
        dtoList.clear()
        dtoList.add(result.rootDTO)
        return  result
    }


}

class ResultSingle<DTO, DATA>(
    internal var rootDTO: CommonDTO<DTO, DATA, LongEntity>
) where DTO : ModelDTO, DATA: DataModel {

    fun getData(): DATA {
        val dataModel =  rootDTO.dataModel
        return dataModel
    }
    fun getDTO(): CommonDTO<DTO, DATA, LongEntity>? {
        return rootDTO
    }


}