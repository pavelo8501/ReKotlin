package po.exposify.classes.components

import kotlinx.io.IOException
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import po.db.data_service.binder.PropertyBinding
import po.db.data_service.binder.UpdateMode
import po.db.data_service.components.eventhandler.EventHandler
import po.db.data_service.components.eventhandler.interfaces.CanNotify
import po.db.data_service.classes.DTOClass
import po.db.data_service.classes.interfaces.DataModel
import po.db.data_service.exceptions.ExceptionCodes
import po.db.data_service.exceptions.OperationsException
import po.db.data_service.dto.DTOBase
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

    fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity> saveNew(
        dto: DTOBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>,
        block: ((ENTITY) -> Unit)? = null): ENTITY? {
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

    fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity> updateExistent(
        dto : DTOBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>) {
        try {
            val entity = selectWhere(dto.id)
            dto.update(entity, UpdateMode.MODEL_TO_ENTNTY)
        }catch (ex: Exception){
            println(ex.message)
        }
    }

    fun pick(
        conditions: List<Pair<KProperty1<DATA, *>, Any?>>,
        propertyBindings: List<PropertyBinding<DATA, ENTITY, *>>
    ): ENTITY?{
        try {
            val entity = notify("pick ") {
                val query = conditionsToSqlBuilderFn<DATA, ENTITY>(conditions, propertyBindings, entityModel)
                entityModel.find(query).firstOrNull()
            }
            return entity
        }catch (ex: IOException){
            println(ex.message)
            throw ex
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

    fun <CHILD_DATA : DataModel, CHILD_ENTITY : LongEntity>delete(
        dto : DTOBase<DATA, ENTITY, CHILD_DATA, CHILD_ENTITY>
    )
    {
        dto.entityDAO.delete()
    }

}