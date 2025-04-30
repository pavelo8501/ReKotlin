package po.exposify.dto.components

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntity


data class CrudResult<DTO, DATA>(
   internal val rootDTOs: List<CommonDTO<DTO, DATA, ExposifyEntity>>,
) where DTO : ModelDTO, DATA: DataModel {

    fun getData(): List<DATA> {
        val dataModels =  rootDTOs.map { it.dataModel }
        return dataModels
    }

    fun getDTO(): List<CommonDTO<DTO, DATA, ExposifyEntity>> {
        return rootDTOs
    }

}

data class CrudResultSingle<DTO, DATA>(
    internal var rootDTO: CommonDTO<DTO, DATA, ExposifyEntity>,
) where DTO : ModelDTO, DATA: DataModel {


    internal fun provideResult(dto: CommonDTO<DTO, DATA, ExposifyEntity>): CrudResultSingle<DTO, DATA>{
        rootDTO = dto
        return this
    }


    fun getData(): DATA {
        val dataModel =  rootDTO.dataModel
        return dataModel
    }

    fun getDTO(): CommonDTO<DTO, DATA, *> {
        return rootDTO
    }

}