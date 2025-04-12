package po.exposify.dto.components

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode



data class CrudResult<DTO, DATA>(
   internal val rootDTOs: List<CommonDTO<DTO, DATA, ExposifyEntityBase>>,
) where DTO : ModelDTO, DATA: DataModel {

    fun getData(): List<DATA> {
        val dataModels =  rootDTOs.map { it.dataModel }
        return dataModels
    }

    fun getDTO(): List<CommonDTO<DTO, DATA, ExposifyEntityBase>> {
        return rootDTOs
    }

}

data class CrudResultSingle<DTO, DATA>(
    internal val rootDTO: CommonDTO<DTO, DATA, ExposifyEntityBase>,
) where DTO : ModelDTO, DATA: DataModel {

    fun getData(): DATA {
        val dataModel =  rootDTO.dataModel
        return dataModel
    }

    fun getDTO(): CommonDTO<DTO, DATA, ExposifyEntityBase> {
        return rootDTO
    }

}