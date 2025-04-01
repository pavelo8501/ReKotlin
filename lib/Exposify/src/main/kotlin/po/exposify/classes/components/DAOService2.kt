package po.exposify.classes.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.and
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.classes.DTOClass2
import po.exposify.dto.interfaces.ModelDTO


class DAOService2<DTO, ENTITY>(
    private val entityModel: LongEntityClass<ENTITY>,
    private val parent : DTOClass2<DTO>
) where DTO: ModelDTO, ENTITY : LongEntity {

    init {
//        eventHandler.registerPropagateException<OperationsException> {
//            OperationsException("Operations Exception", ExceptionCodes.INVALID_DATA)
//        }
    }

    private fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> {
        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
    }

    suspend fun saveNew(
        dto: CommonDTO<DTO, *, ENTITY>,
        block: ((ENTITY) -> Unit)? = null): ENTITY? {
        // Notify about the operation

            // Create a new entity and update its properties
            val newEntity = entityModel.new {
               // dto.update(this, UpdateMode.MODEL_TO_ENTITY)
              //  block?.invoke(this)
            }
          return newEntity
    }

    suspend fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity> updateExistent(
        dto : CommonDTO<DTO, *, ENTITY>) {
        try {
            val entity = selectById(dto.id)
          //  dto.update(entity, UpdateMode.MODEL_TO_ENTITY)
        }catch (ex: Exception){
            println(ex.message)
        }
    }

    fun pick(
        conditions :Op<Boolean>
    ): ENTITY?{
        val queryResult = entityModel.find(conditions)
        return queryResult.firstOrNull()
    }

    suspend fun select(
        conditions: Op<Boolean>,
    ): List<ENTITY>{
        //val entities = task("select_with_conditions") {
            return entityModel.find(conditions).toList()
       // }
       // return entities?:emptyList()
    }

    private fun loadAllChildren(entityModel : LongEntityClass<ENTITY>, entity: ENTITY){
        entityModel.reload(entity)
    }

    suspend fun selectByQuery(
        query: Query
    ): List<ENTITY>{
       // val entities = task("select_with_query") {
            val result =   entityModel.wrapRows(query).toList()
            result.forEach { loadAllChildren(entityModel, it) }
            return result
       // }
       // return entities?:emptyList()
    }

    suspend fun selectAll(): SizedIterable<ENTITY>{
       // val entities = task("selectAll() for dtoModel ${parent.personalName}") {
           return entityModel.all()
      //  }
       // return entities!!
    }

    suspend fun selectById(id: Long): ENTITY?{
      //  if(id == 0L)  throwPropagate("Id should be greater than 0")
      //  val entity = task("selectWhere for dtoModel ${parent.personalName}") {
          return  entityModel.findById(id)
      //  }
      //  return entity!!
    }

    suspend fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>delete(
        dto : CommonDTO<DTO, *, ENTITY>
    ) {
       // task("selectWhere for dtoModel"){

      //  }
        dto.entityDAO.delete()
    }

}