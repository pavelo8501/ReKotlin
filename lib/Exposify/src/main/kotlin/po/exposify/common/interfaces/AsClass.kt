package po.exposify.common.interfaces

import org.jetbrains.exposed.dao.LongEntity
import po.exposify.dto.interfaces.DataModel


interface AsClass<DATA: DataModel, ENTITY: LongEntity> {
   // val dataModel: DATA
    //val entityModel : LongEntityClass<ENTITY>
}