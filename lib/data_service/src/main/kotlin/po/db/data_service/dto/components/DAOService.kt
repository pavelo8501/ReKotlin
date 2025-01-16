package po.db.data_service.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.SizedIterable
import po.db.data_service.binder.UpdateMode
import po.db.data_service.components.eventhandler.EventHandler
import po.db.data_service.components.eventhandler.interfaces.CanNotify
import po.db.data_service.dto.DTOClass
import po.db.data_service.dto.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.models.EntityDTO

abstract class DAOService<DATA, ENTITY>(
  private val parent : DTOClass<DATA, ENTITY>
): CanNotify where  DATA : DataModel,  ENTITY : LongEntity {

   override val eventHandler : EventHandler = EventHandler("DAOService", parent.eventHandler)

    val entityModel : LongEntityClass<ENTITY>
        get(){return  parent.entityModel}


    fun saveNew(
        dto : EntityDTO<DATA, ENTITY>,
        block: ((ENTITY)-> Unit)? = null
    ): ENTITY? {
        try {
            val newEntity = entityModel.new {
                dto.update(this, UpdateMode.MODEL_TO_ENTNTY)
                block?.invoke(this)
            }
            return newEntity
        }catch (ex: Exception){
            println(ex.message)
            return null
        }
    }

    fun updateExistent(
        dto : EntityDTO<DATA, ENTITY>,
        entityModel: LongEntityClass<ENTITY>
    ){
        try {
            val entity = selectWhere(dto.id, entityModel)
            dto.update(entity, UpdateMode.MODEL_TO_ENTNTY)
        }catch (ex: Exception){
            println(ex.message)
        }
    }

    fun selectAll(
        entityModel: LongEntityClass<ENTITY>
    ): SizedIterable<ENTITY>{
        try {
            return entityModel.all()
        }catch (ex: Exception){
            println(ex.message)
            throw ex
        }
    }

    fun  selectWhere(
        id: Long,  entityModel: LongEntityClass<ENTITY>
    ): ENTITY{
        if(id == 0L) throw OperationsException("Id should be greater than 0", ExceptionCodes.INVALID_DATA)
        val entity = entityModel[id]
        return entity
    }

}