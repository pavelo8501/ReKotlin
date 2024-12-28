package po.db.data_service.scope.service.interfaces

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.DTOClass

interface ServiceInterface<ENTITY : LongEntity> {

    fun initDtoModel():DTOClass<ENTITY>

}