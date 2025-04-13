package po.exposify.common.interfaces

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import po.exposify.classes.interfaces.DataModel
import kotlin.reflect.KClass


interface AsClass<DATA: DataModel, ENTITY: LongEntity> {
   // val dataModel: DATA
    //val entityModel : LongEntityClass<ENTITY>
}