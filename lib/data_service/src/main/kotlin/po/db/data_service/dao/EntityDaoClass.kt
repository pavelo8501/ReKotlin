package po.db.data_service.dao

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.dto.DataTransferObjectsParent
import po.db.data_service.dto.MarkerInterface


open class EntityDaoClass(
    val longEntityCompanion : LongEntity,
    //val dataTransferObjectsCompanion : DataTransferObjectsClass
) // where ENTITY : LongEntity, DATA_MODEL: MarkerInterface
{


}

