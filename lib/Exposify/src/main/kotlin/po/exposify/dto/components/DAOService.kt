package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import po.exposify.dto.CommonDTO
import po.exposify.dto.components.property_binder.enums.UpdateMode
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.enums.ExceptionCode
import po.exposify.extensions.WhereCondition
import po.exposify.extensions.getOrOperationsEx
import po.lognotify.TasksManaged
import po.lognotify.extensions.resultOrNull
import po.lognotify.extensions.subTask

class DAOService<DTO, ENTITY>(
    private val hostingDTO: CommonDTO<DTO, *, ENTITY>,
    val entityModel: LongEntityClass<ENTITY>,
): TasksManaged where DTO: ModelDTO, ENTITY : ExposifyEntityBase {

    private val personalName = "DAOService[${hostingDTO.personalName}]"
    var entity : ENTITY? = null
        private set

    internal fun getLastEntity():ENTITY{
        return entity.getOrOperationsEx("Entity is null", ExceptionCode.NOT_INITIALIZED)
    }

    private fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> {
        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
    }

    private  fun  <T:IdTable<Long>> buildConditions(conditions: WhereCondition<T>): Op<Boolean> {
        val conditions = conditions.build()
        return conditions
    }

    private var beforeEntityInsertedHook :  ((ENTITY)-> Unit)? = null
    fun  setBeforeInsertedHook(hook :(ENTITY)-> Unit){
        beforeEntityInsertedHook = hook
    }

    private var afterEntityInsertedUpdateHook :  ((ENTITY)-> Unit)? = null
    fun setAfterInsertedHook(hook :(ENTITY)-> Unit){
        afterEntityInsertedUpdateHook = hook
    }

    internal fun setActiveEntity(processedEntity:ENTITY){
        entity = processedEntity
        afterEntityInsertedUpdateHook?.invoke(processedEntity)

    }

   suspend fun  <T:IdTable<Long>> pick(conditions :  WhereCondition<T>): ENTITY? = subTask("Pick", personalName){handler->
       val opConditions = buildConditions(conditions)
       val queryResult = entityModel.find(opConditions).firstOrNull()
        if(queryResult == null){
            handler.warn("Entity  not found")
        }
        queryResult
    }.resultOrNull()

    suspend fun pickById(id: Long): ENTITY? =  subTask("PickById", personalName) {handler->
      val entity =  entityModel.findById(id)
      if(entity == null){
          handler.warn("Entity with id: $id not found")
      }
      entity
    }.resultOrNull()


    suspend fun select(): List<ENTITY> = subTask("Select All", personalName){
        entityModel.all().toList()
    }.resultOrException()

    suspend fun <T:IdTable<Long>> select(conditions:  WhereCondition<T>): List<ENTITY> =
        subTask("Select", personalName) {handler->
        val opConditions = buildConditions(conditions)
        val result = entityModel.find(opConditions).toList()
        handler.info("${result.count()} entities selected")
        result
    }.resultOrException()

    suspend fun save(): ENTITY =
        subTask("Save", "DAOService") {handler->
        val newEntity = entityModel.new {
            beforeEntityInsertedHook?.invoke(this)
            hostingDTO.updateBinding(this, UpdateMode.MODEL_TO_ENTITY)
        }
        hostingDTO.dataContainer.setDataModelId(newEntity.id.value)
        setActiveEntity(newEntity)
        handler.info("Dao entity created with id ${newEntity.id.value} for dto ${hostingDTO.personalName}")
        newEntity
    }.resultOrException()

    suspend fun saveWithParent(bindFn: (newEntity:ENTITY)-> Unit):ENTITY =
        subTask("Save", personalName) {handler->
            val newEntity = entityModel.new {
                beforeEntityInsertedHook?.invoke(this)
                hostingDTO.updateBinding(this, UpdateMode.MODEL_TO_ENTITY)
                bindFn.invoke(this)
            }
            hostingDTO.dataContainer.setDataModelId(newEntity.id.value)
            setActiveEntity(newEntity)
            handler.info("Entity created with id: ${newEntity.id.value} for parent entity id:")
            newEntity
    }.resultOrException()

    suspend fun update(): ENTITY = subTask("Update", "DAOService") {handler->
        val selectedEntity =  pickById(hostingDTO.id).getOrOperationsEx("Entity with id : ${hostingDTO.id} not found", ExceptionCode.DB_CRUD_FAILURE)
        beforeEntityInsertedHook?.invoke(selectedEntity)
        hostingDTO.updateBinding(selectedEntity, UpdateMode.MODEL_TO_ENTITY)
        setActiveEntity(selectedEntity)
        selectedEntity
    }.resultOrException()



//    private fun loadAllChildren(entityModel : LongEntityClass<ENTITY>, entity: ENTITY){
//        entityModel.reload(entity)
//    }
//
//    suspend fun selectByQuery(
//        query: Query
//    ): List<ENTITY>{
//       // val entities = task("select_with_query") {
//            val result =   entityModel.wrapRows(query).toList()
//            result.forEach { loadAllChildren(entityModel, it) }
//            return result
//       // }
//       // return entities?:emptyList()
//    }


//    suspend fun select(conditions: Op<Boolean>, ): List<ENTITY>{
//        //val entities = task("select_with_conditions") {
//        return entityModel.find(conditions).toList()
//        // }
//        // return entities?:emptyList()
//    }
//
//    suspend fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>delete(
//        dto : CommonDTO<DTO, DataModel, ENTITY>
//    ) {
//       // task("selectWhere for dtoModel"){
//
//      //  }
//        dto.entityDAO.delete()
//    }

}