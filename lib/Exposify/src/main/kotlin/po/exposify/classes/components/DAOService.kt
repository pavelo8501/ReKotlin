package po.exposify.classes.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SizedIterable
import po.exposify.binders.UpdateMode
import po.exposify.classes.interfaces.DataModel
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.ExceptionCodes
import po.lognotify.eventhandler.EventHandler
import po.lognotify.eventhandler.interfaces.CanNotify

//class DAOService<DATA, ENTITY>(
//  private val parent : DTOClass<DATA, ENTITY>
//): CanNotify where  DATA : DataModel,  ENTITY : LongEntity {
//
//   override val eventHandler : EventHandler = EventHandler("DAOService", parent.eventHandler)
//
//   val entityModel : LongEntityClass<ENTITY>
//        get(){return  parent.entityModel}
//
//    init {
//        eventHandler.registerPropagateException<OperationsException> {
//            OperationsException("Operations Exception", ExceptionCodes.INVALID_DATA)
//        }
//    }
//
//    private fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> {
//        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
//    }
//
//    suspend fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity> saveNew(
//
//        block: ((ENTITY) -> Unit)? = null): ENTITY? {
//            // Notify about the operation
//            val entity = task("saveNew() for dto ${dto.dtoClass.personalName}") {
//                // Create a new entity and update its properties
//                val newEntity = entityModel.new {
//                  //  dto.update(this, UpdateMode.MODEL_TO_ENTITY)
//                 //   block?.invoke(this)
//                }
//                newEntity
//            }
//           return entity!!
//    }
//
//    suspend fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity> updateExistent(
//        dto : DTOBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>) {
//        try {
//            val entity = selectWhere(dto.id)
//            dto.updateBinding(entity, UpdateMode.MODEL_TO_ENTITY)
//        }catch (ex: Exception){
//            println(ex.message)
//        }
//    }
//
//    suspend fun pick(
//        conditions :Op<Boolean>
//    ): ENTITY?{
//        val entity = task("pick") {
//            val queryResult = entityModel.find(conditions)
//            queryResult.firstOrNull()
//        }
//        return entity
//    }
//
//    suspend fun select(
//        conditions: Op<Boolean>,
//    ): List<ENTITY>{
//        val entities = task("select_with_conditions") {
//            entityModel.find(conditions).toList()
//        }
//        return entities?:emptyList()
//    }
//
//    private fun loadAllChildren(entityModel : LongEntityClass<ENTITY>, entity: ENTITY){
//        entityModel.reload(entity)
//    }
//
//    suspend fun selectByQuery(
//        query: Query
//    ): List<ENTITY>{
//        val entities = task("select_with_query") {
//           val result =   entityModel.wrapRows(query).toList()
//           result.forEach { loadAllChildren(entityModel, it) }
//           result
//        }
//        return entities?:emptyList()
//    }
//
//    suspend fun selectAll(): SizedIterable<ENTITY>{
//       val entities = task("selectAll() for dtoModel ${parent.personalName}") {
//            entityModel.all()
//        }
//        return entities!!
//    }
//
//    suspend fun selectWhere(id: Long): ENTITY{
//        if(id == 0L)  throwPropagate("Id should be greater than 0")
//        val entity = task("selectWhere for dtoModel ${parent.personalName}") {
//            entityModel[id]
//        }
//        return entity!!
//    }
//
//    suspend fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>delete(
//        dto : DTOBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
//    ) {
//        task("selectWhere for dtoModel"){
//
//        }
//        dto.entityDAO.delete()
//    }
//
//}