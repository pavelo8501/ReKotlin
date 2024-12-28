package po.db.data_service.dto.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO

class DTOResult<ENTITY>(private val dtoModel : DTOClass<ENTITY>) where ENTITY:LongEntity {


    fun getList():List<CommonDTO>{
        return dtoModel.dtoContainer
    }

    fun getDataModelList():List<DataModel>{
        return  emptyList()
    }

}