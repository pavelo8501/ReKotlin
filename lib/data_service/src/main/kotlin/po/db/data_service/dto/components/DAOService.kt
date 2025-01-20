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
import po.db.data_service.models.CommonDTO
import po.db.data_service.models.DTOBase

class DAOService<DATA, ENTITY>(
  private val parent : DTOClass<DATA, ENTITY>
): CanNotify where  DATA : DataModel,  ENTITY : LongEntity {

   override val eventHandler : EventHandler = EventHandler("DAOService", parent.eventHandler)

   val entityModel : LongEntityClass<ENTITY>
        get(){return  parent.entityModel}

//    fun <CHILD_ENTITY: LongEntity>saveNew(dto : DTOBase<*, CHILD_ENTITY, *, ENTITY>, block: ((CHILD_ENTITY)-> Unit)? = null): CHILD_ENTITY? {
//        try {
//            val entity = notify("saveNew() for dto ${dto.sourceModel.className}"){
//                val newEntity = entityModel.new {
//                    dto.update(this, UpdateMode.MODEL_TO_ENTNTY)
//                    block?.invoke(this)
//                }
//                newEntity
//            }
//            return entity
//        }catch (ex: Exception){
//            notifyError(ex.message?:"Unknown Exception")
//            return null
//        }
//    }

    fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity> saveNew(
        dto: DTOBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
        block: ((ENTITY) -> Unit)? = null
    ): ENTITY? {
            // Notify about the operation
            val entity = notify("saveNew() for dto ${dto.sourceModel.className}") {
                // Create a new entity and update its properties
                val newEntity = entityModel.new {
                    dto.update(this, UpdateMode.MODEL_TO_ENTNTY)
                    block?.invoke(this)
                }
                newEntity
            }
           return entity!!
    }

    fun updateExistent(dto : CommonDTO<DATA, ENTITY>){
        try {
            val entity = selectWhere(dto.id)
            dto.update(entity, UpdateMode.MODEL_TO_ENTNTY)
        }catch (ex: Exception){
            println(ex.message)
        }
    }

    fun selectAll(): SizedIterable<ENTITY>{
        try {
           val entities = notify("selectAll() for dtoModel ${parent.className}") {
                entityModel.all()
            }
            return entities!!
        }catch (ex: Exception){
            println(ex.message)
            throw ex
        }
    }

    fun  selectWhere(id: Long): ENTITY{
        if(id == 0L) throw OperationsException("Id should be greater than 0", ExceptionCodes.INVALID_DATA)
        val entity = notify("selectAll() for dtoModel ${parent.className}") {
            entityModel[id]
        }
        return entity!!
    }

    fun delete(dto : CommonDTO<DATA, ENTITY>){
        dto.entityDAO.delete()
    }

}