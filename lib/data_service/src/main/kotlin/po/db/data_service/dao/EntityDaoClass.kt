package po.db.data_service.dao

import org.jetbrains.exposed.dao.LongEntity



open class EntityDaoClass(
    val longEntityCompanion : LongEntity,
    //val dataTransferObjectsCompanion : DataTransferObjectsClass
) // where ENTITY : LongEntity, DATA_MODEL: MarkerInterface
{


}

