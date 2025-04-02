package po.exposify.classes.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.and
import po.exposify.binders.UpdateMode
import po.exposify.binders.enums.Cardinality
import po.exposify.binders.relationship.models.EntityPropertyInfo
import po.exposify.classes.interfaces.DataModel
import po.exposify.dto.CommonDTO
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.entity.classes.ExposifyEntityBase
import po.exposify.exceptions.ExceptionCodes
import po.exposify.exceptions.OperationsException


class DAOService2<DTO, DATA, ENTITY>(
    val isRootDaoService: Boolean,
    private val entityModel: LongEntityClass<ENTITY>,
) where DTO: ModelDTO, DATA: DataModel, ENTITY : ExposifyEntityBase {

    internal var entity : ENTITY? = null

    val trackedProperties: MutableMap<String, EntityPropertyInfo<DTO, DATA, ENTITY, ModelDTO>> = mutableMapOf()

    private fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> {
        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
    }

    fun setTrackedProperties(list: List<EntityPropertyInfo<DTO, DATA, ENTITY, ModelDTO>>){
        list.forEach {propertyInfo->
            trackedProperties[propertyInfo.name] = propertyInfo
        }
    }

    fun extractChildEntities(ownEntitiesPropertyInfo : EntityPropertyInfo<DTO, DATA, ENTITY, ModelDTO>): List<ExposifyEntityBase> {

        val parentEntity = entity
            if (ownEntitiesPropertyInfo.cardinality == Cardinality.ONE_TO_MANY) {
                val multipleProperty = ownEntitiesPropertyInfo.getOwnEntitiesProperty()
                if (parentEntity != null && multipleProperty != null) {
                    return multipleProperty.get(parentEntity).toList()
                } else {
                    throw OperationsException(
                        "Property for name ${ownEntitiesPropertyInfo.name} not found in trackedProperties. Searching ONE_TO_MANY",
                        ExceptionCodes.BINDING_PROPERTY_MISSING
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
                            ExceptionCodes.BINDING_PROPERTY_MISSING
                        )
                    }
                }
            }
        return emptyList()
    }


    suspend fun save(dto: CommonDTO<DTO, DATA, ENTITY>): ENTITY? {
        runCatching {
            val newEntity = entityModel.new {
                dto.updateBinding(this, UpdateMode.MODEL_TO_ENTITY)
            }
            entity = newEntity
            return newEntity
        }.onFailure {
            throw OperationsException("Save failed for", ExceptionCodes.DB_CRUD_FAILURE)
        }
        return null
    }

    suspend fun saveWithParent(dto: CommonDTO<DTO, DATA, ENTITY>, bindFn: (ENTITY)-> Unit):ENTITY?{
        runCatching {
            val newEntity = entityModel.new {
                dto.updateBinding(this, UpdateMode.MODEL_TO_ENTITY)
                bindFn.invoke(this)
            }
            entity = newEntity
            return newEntity
        }.onFailure {
            throw OperationsException("SaveWithParent failed for", ExceptionCodes.DB_CRUD_FAILURE)
        }
        return null
    }

    suspend fun update(dto : CommonDTO<DTO, DATA, ENTITY>): ENTITY? {
        val selectedEntity = selectById(dto.id)
        if(selectedEntity != null){
            dto.updateBinding(selectedEntity, UpdateMode.MODEL_TO_ENTITY)
        }
        entity = selectedEntity
        return entity
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
        dto : CommonDTO<DTO, DATA, ENTITY>
    ) {
       // task("selectWhere for dtoModel"){

      //  }
        dto.entityDAO.delete()
    }

}