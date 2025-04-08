package po.exposify.dto.components

import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase

data class CrudResult<DTO, DATA>(
   internal val rootDTOs: List<CommonDTO<DTO, DATA, ExposifyEntityBase>>,
) where DTO : ModelDTO, DATA: DataModel {


    fun getDataModels(): List<DATA> {
        val dataModels =  rootDTOs.map { it.dataModel }
        return dataModels
    }
}