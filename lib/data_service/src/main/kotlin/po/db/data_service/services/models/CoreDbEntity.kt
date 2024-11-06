package po.db.data_service.services.models

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

interface CoreDbEntityContext{
    companion object : CoreDbEntityClass()
}

open class CoreDbEntityClass{
    var sourceCompanion : LongEntityClass<*>? = null

    fun setCompanion(source: LongEntityClass<*>){
        sourceCompanion = source
    }

    fun getCompanion(source: LongEntityClass<*>):LongEntityClass<*>{
        if(sourceCompanion == null){
            throw IllegalStateException("Companion not set for ${source::class.simpleName}")
        }
        return sourceCompanion!!
    }
}

abstract class CoreDbEntity(id:  EntityID<Long>): LongEntity(id), CoreDbEntityContext{

    init {
        if (this::class.objectInstance == null) {
            throw IllegalStateException("This abstract class can only be extended by a singleton object.")
        }else{
            CoreDbEntityContext.setCompanion(this::class.objectInstance as LongEntityClass<*>)
        }
    }
}
