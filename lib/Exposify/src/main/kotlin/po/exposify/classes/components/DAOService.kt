package po.exposify.classes.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import po.exposify.binder.PropertyBinding
import po.exposify.binder.UpdateMode
import po.exposify.classes.DTOClass
import po.exposify.classes.interfaces.DataModel
import po.exposify.exceptions.OperationsException
import po.exposify.dto.DTOBase
import po.lognotify.eventhandler.EventHandler
import po.lognotify.eventhandler.interfaces.CanNotify
import kotlin.reflect.KProperty1

class DAOService<DATA, ENTITY>(
  private val parent : DTOClass<DATA, ENTITY>
): CanNotify where  DATA : DataModel,  ENTITY : LongEntity {

   override val eventHandler : EventHandler = EventHandler("DAOService", parent.eventHandler)

   val entityModel : LongEntityClass<ENTITY>
        get(){return  parent.entityModel}

    private fun <DATA : DataModel, ENT : LongEntity> conditionsToSqlBuilderFn(
        conditions: List<Pair<KProperty1<DATA, *>, Any?>>,
        propertyBindings: List<PropertyBinding<DATA, ENT, *>>,
        entityModel : LongEntityClass<ENTITY>
    ): SqlExpressionBuilder.() -> Op<Boolean> {
        return {
            conditions.mapNotNull { (dtoProp, value) ->
                val binding = propertyBindings.find { it.dtoProperty == dtoProp }
                @Suppress("UNCHECKED_CAST")
                val column = binding?.let {
                    entityModel.table.columns.find { col -> col.name == binding.entityProperty.name }
                }as? Column<Any?>
                column?.let { it eq value }
            }.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
        }
    }

    suspend fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity> saveNew(
        dto: DTOBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
        block: ((ENTITY) -> Unit)? = null): ENTITY? {
            // Notify about the operation
            val entity = action("saveNew() for dto ${dto.sourceModel.className}") {
                // Create a new entity and update its properties
                val newEntity = entityModel.new {
                    dto.update(this, UpdateMode.MODEL_TO_ENTNTY)
                    block?.invoke(this)
                }
                newEntity
            }
           return entity!!
    }

    suspend fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity> updateExistent(
        dto : DTOBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>) {
        try {
            val entity = selectWhere(dto.id)
            dto.update(entity, UpdateMode.MODEL_TO_ENTNTY)
        }catch (ex: Exception){
            println(ex.message)
        }
    }

    suspend fun pick(
        conditions: List<Pair<KProperty1<DATA, *>, Any?>>,
        propertyBindings: List<PropertyBinding<DATA, ENTITY, *>>
    ): ENTITY?{

        val entity = action("pick") {
            val query = conditionsToSqlBuilderFn<DATA, ENTITY>(conditions, propertyBindings, entityModel)
            entityModel.find(query).firstOrNull()
        }
        return entity
    }

    suspend fun selectAll(): SizedIterable<ENTITY>{
       val entities = action("selectAll() for dtoModel ${parent.className}") {
            entityModel.all()
        }
        return entities!!
    }

    suspend fun selectWhere(id: Long): ENTITY{
        if(id == 0L)  throwPropagated<OperationsException>("Id should be greater than 0")
        val entity = action("selectWhere for dtoModel ${parent.className}") {
            entityModel[id]
        }
        return entity!!
    }

    suspend fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>delete(
        dto : DTOBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
    ) {
        action("selectWhere for dtoModel"){

        }
        dto.entityDAO.delete()
    }

}