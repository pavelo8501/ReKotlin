package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.and
import po.exposify.binders.UpdateMode
import po.exposify.binders.enums.Cardinality
import po.exposify.binders.relationship.models.EntityPropertyInfo
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.OperationsException
import po.exposify.exceptions.enums.ExceptionCode
import po.lognotify.TasksManaged
import po.lognotify.extensions.getOrThrowDefault
import po.lognotify.extensions.onFailureCause
import po.lognotify.extensions.subTask

class DAOService<DTO, ENTITY>(
    private val hostingDTO: CommonDTO<DTO, *, ENTITY>,
    private val entityModel: LongEntityClass<ENTITY>,
): TasksManaged where DTO: ModelDTO,  ENTITY : ExposifyEntityBase {

    private val personalName = "DAOService"

    private var entity : ENTITY? = null

    private  fun setNewEntity(newEntity:ENTITY?){
        entity = newEntity
    }

    internal  fun getLastEntity():ENTITY{
        return entity.getOrThrowDefault("Entity is null")
    }

    internal var transaction : Transaction? = null

    val trackedProperties: MutableMap<String, EntityPropertyInfo<DTO, DataModel, ENTITY, ModelDTO>> = mutableMapOf()

    private fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> {
        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
    }

    fun setTrackedProperties(list: List<EntityPropertyInfo<DTO, DataModel, ENTITY, ModelDTO>>){
        list.forEach {propertyInfo->
            trackedProperties[propertyInfo.name] = propertyInfo
        }
    }

    fun extractChildEntities(ownEntitiesPropertyInfo : EntityPropertyInfo<DTO, DataModel, ENTITY, ModelDTO>): List<ExposifyEntityBase> {
        val parentEntity = entity
            if (ownEntitiesPropertyInfo.cardinality == Cardinality.ONE_TO_MANY) {
                val multipleProperty = ownEntitiesPropertyInfo.getOwnEntitiesProperty()
                if (parentEntity != null && multipleProperty != null) {
                    return multipleProperty.get(parentEntity).toList()
                } else {
                    throw OperationsException(
                        "Property for name ${ownEntitiesPropertyInfo.name} not found in trackedProperties. Searching ONE_TO_MANY",
                        ExceptionCode.BINDING_PROPERTY_MISSING
                    )
                }
                if (ownEntitiesPropertyInfo.cardinality == Cardinality.ONE_TO_ONE) {
                    val property = ownEntitiesPropertyInfo.getOwnEntityProperty()
                    if (property != null && parentEntity != null) {
                        val entity = property.get(parentEntity)
                        return listOf<ExposifyEntityBase>(entity)
                    } else {
                        throw OperationsException(
                            "Property for name ${ownEntitiesPropertyInfo.name} not found in trackedProperties. Searching ONE_TO_ONE",
                            ExceptionCode.BINDING_PROPERTY_MISSING
                        )
                    }
                }
            }
        return emptyList()
    }

    suspend fun save(): ENTITY =
        subTask("Save", "DAOService") {handler->
        val newEntity = entityModel.new {
            hostingDTO.updateBinding(this, UpdateMode.MODEL_TO_ENTITY)
        }
        hostingDTO.dataContainer.setDataModelId(newEntity.id.value)
        handler.info("Dao entity created with id ${newEntity.id.value} for dto ${hostingDTO.personalName}")
        setNewEntity(newEntity)
        newEntity
    }.resultOrException()

    suspend fun saveWithParent(bindFn: (newEntity:ENTITY)-> Unit):ENTITY =
        subTask("Save", personalName) {handler->
            val newEntity = entityModel.new {
                hostingDTO.updateBinding(this, UpdateMode.MODEL_TO_ENTITY)
                bindFn.invoke(this)
            }
            hostingDTO.dataContainer.setDataModelId(newEntity.id.value)
            handler.info("Entity created with id: ${newEntity.id.value} for parent entity id:")
            setNewEntity(newEntity)
            newEntity
    }.resultOrException("SaveWithParent failed for ${hostingDTO.personalName}")

    suspend fun update(): ENTITY =
        subTask("Update", "DAOService") {handler->
        val selectedEntity =  selectById(hostingDTO.id).getOrThrowDefault("Entity with id : ${hostingDTO.id} not found")
        hostingDTO.updateBinding(selectedEntity, UpdateMode.MODEL_TO_ENTITY)
        setNewEntity(selectedEntity)
        selectedEntity
    }.resultOrException("SaveWithParent failed for ${hostingDTO.personalName}")

    fun pick(
        conditions : Op<Boolean>
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

    suspend fun selectAll(): SizedIterable<ENTITY> {
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
        dto : CommonDTO<DTO, DataModel, ENTITY>
    ) {
       // task("selectWhere for dtoModel"){

      //  }
        dto.entityDAO.delete()
    }

}