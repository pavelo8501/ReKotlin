package po.db.data_service.services.models

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID



open class CoreDbEntityClass{
    var sourceCompanion : LongEntityClass<*>? = null

    fun setCompanion(source: LongEntityClass<*>){
        sourceCompanion = source
    }
}

interface CoreDbEntityContext{
    companion object : CoreDbEntityClass()
}



abstract class CoreDbEntity(id:  EntityID<Long>): LongEntity(id), CoreDbEntityContext{

    init {
        if (this::class.objectInstance != null) {
            CoreDbEntityContext.setCompanion(this::class.objectInstance as LongEntityClass<*>)
        }
    }
}
