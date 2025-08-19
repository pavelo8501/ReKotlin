package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.flushCache
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import po.exposify.common.classes.ExposifyDebugger
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dao.helpers.hasChanges
import po.exposify.dto.components.query.SimpleQuery
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dto.models.CommonDTOType
import po.exposify.extensions.currentTransaction
import po.exposify.extensions.withSuspendedTransactionIfNone
import po.lognotify.TasksManaged
import po.lognotify.launchers.runAction
import po.misc.context.CTXIdentity
import po.misc.context.asIdentity
import po.misc.data.helpers.output
import po.misc.data.processors.SeverityLevel
import po.misc.data.styles.Colour
import kotlin.reflect.full.withNullability

class DAOService<DTO, DATA, ENTITY>(
    val commonDTOType: CommonDTOType<DTO, DATA, ENTITY>,
) : TasksManaged where DTO : ModelDTO, DATA : DataModel, ENTITY : LongEntity {
    override val identity: CTXIdentity<DAOService<DTO, DATA, ENTITY>> = asIdentity()

    val entityClass: ExposifyEntityClass<ENTITY> get() = commonDTOType.entityType.entityClass
    private val entityType get() = commonDTOType.entityType

    private fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> = conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE

    private fun buildConditions(conditions: SimpleQuery): Op<Boolean> {
        val conditions = conditions.build()
        return conditions
    }

    fun pick(conditions: SimpleQuery): ENTITY? =
        runAction("Pick", commonDTOType.entityType.kType.withNullability(true)) {
            val opConditions = buildConditions(conditions)
            val queryResult = entityClass.find(opConditions).firstOrNull()
            queryResult
        }

    fun pickById(id: Long): ENTITY? =
        runAction("PickById", commonDTOType.entityType.kType.withNullability(true)) {
            val entity = entityClass.findById(id)
            if (entity == null) {
                notify("Entity with id: $id not found", SeverityLevel.INFO)
            }
            entity
        }

    fun select(): List<ENTITY> =
        runAction("Select(all)") {
            entityClass.all().toList()
        }

    fun select(conditions: SimpleQuery): List<ENTITY> =
        runAction("Select") {
            val opConditions = buildConditions(conditions)
            val result = entityClass.find(opConditions).toList()
            notify("${result.count()} entities selected", SeverityLevel.INFO)
            result
        }

    fun save(block: (entity: ENTITY) -> Unit): ENTITY {
        val newEntity = entityClass.new {
            block.invoke(this)
        }
       return newEntity
    }


    fun update(entity: ENTITY, updateBlock: (ENTITY) -> Unit): ENTITY = runAction("Update", entityType.kType) {
        updateBlock.invoke(entity)
        entity.flush()
        currentTransaction()?.flushCache()
        entity
    }

    fun update(entity: ENTITY,debugger: ExposifyDebugger<*, *>, updateBlock: (ENTITY) -> Unit): ENTITY = runAction("Update", entityType.kType) {
        updateBlock.invoke(entity)
        val dirtyBeforeFlush = entity.hasChanges()
        entity.flush()
        if (dirtyBeforeFlush) {
            debugger.logSql("Entity ${entity.id.value} flushed with updates")
        } else {
            debugger.warn("No changes detected for entity ${entity.id.value}, flush skipped UPDATE")
        }
        entity
    }

    fun update(
        entityId: Long,
        updateFn: (newEntity: ENTITY) -> Unit,
    ): ENTITY? = runAction("Update", entityType.kType.withNullability(true)) {
        val updated = pickById(entityId)?.let { picked ->
            updateFn.invoke(picked)
            picked
        } ?: run {
            notify("Update failed. Entity with id: $entityId for $completeName can not be found", SeverityLevel.WARNING)
            null
        }
        updated
    }
}
