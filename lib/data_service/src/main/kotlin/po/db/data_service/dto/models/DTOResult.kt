package po.db.data_service.dto.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.HostableDTO

class DTOResult<ENTITY>(private val dtoModel : DTOClass<ENTITY>) where ENTITY:LongEntity {


    fun getList():List<HostableDTO<ENTITY>>{
        return dtoModel.repository.getAll()
    }

    fun getDataModelList():List<DataModel>{
        return  emptyList()
    }

}