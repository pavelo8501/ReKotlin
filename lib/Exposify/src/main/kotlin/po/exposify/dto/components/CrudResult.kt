package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO

class ResultList<DTO, DATA>  (
   private val initialList : List<CommonDTO<DTO, DATA, LongEntity>>? = null
)  where DTO : ModelDTO, DATA: DataModel{

    internal val rootDTOs: MutableList<CommonDTO<DTO, DATA, LongEntity>> = mutableListOf()

    init {
        initialList?.let {
            rootDTOs.addAll(it)
        }
    }


    fun addList(list: List<CommonDTO<DTO, DATA, LongEntity>>):ResultList<DTO, DATA>{
        rootDTOs.addAll(list)
        return  this
    }

    internal fun appendDto(dto:CommonDTO<DTO, DATA, LongEntity>):ResultList<DTO, DATA>{
        rootDTOs.add(dto)
        return  this
    }

    fun getData(): List<DATA> {
        val dataModels =  rootDTOs.map { it.dataModel }
        return dataModels
    }

    fun getDTO(): List<CommonDTO<DTO, DATA, LongEntity>> {
        return rootDTOs
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