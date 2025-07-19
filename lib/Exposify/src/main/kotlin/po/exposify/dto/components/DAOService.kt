package po.exposify.dto.components

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.and
import po.exposify.dto.interfaces.DataModel
import po.exposify.dto.DTOBase
import po.exposify.dto.interfaces.ModelDTO
import po.exposify.dao.classes.ExposifyEntityClass
import po.exposify.dto.components.query.SimpleQuery
import po.lognotify.TasksManaged
import po.lognotify.extensions.runAction
import po.lognotify.extensions.runInlineAction
import kotlin.reflect.full.withNullability


class DAOService<DTO, DATA, ENTITY>(
    val dtoClass: DTOBase<DTO, DATA, ENTITY>
): TasksManaged   where DTO: ModelDTO, DATA: DataModel, ENTITY : LongEntity {

    override val contextName: String = "DAOService"
    val entityModel: ExposifyEntityClass<ENTITY> get() = dtoClass.config.entityModel

    private fun combineConditions(conditions: Set<Op<Boolean>>): Op<Boolean> {
        return conditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
    }

    private fun buildConditions(conditions: SimpleQuery): Op<Boolean> {
        val conditions = conditions.build()
        return conditions
    }

    fun pick(conditions: SimpleQuery): ENTITY? = runAction("Pick", dtoClass.entityType.kType.withNullability(true)) {
        val opConditions = buildConditions(conditions)
        val queryResult = entityModel.find(opConditions).firstOrNull()
        queryResult
    }

    fun pickById(id: Long): ENTITY? = runAction("PickById", dtoClass.entityType.kType.withNullability(true)){
        val entity = entityModel.findById(id)
        if (entity == null) {
            info("Entity with id: $id not found")
        }
        entity
    }

    fun select(): List<ENTITY> = runInlineAction("Select(all)") {
        entityModel.all().toList()
    }

    fun select(conditions: SimpleQuery): List<ENTITY> = runInlineAction("Select") {
        val opConditions = buildConditions(conditions)
        val result = entityModel.find(opConditions).toList()
        info("${result.count()} entities selected")
        result
    }


    fun save(block: (entity: ENTITY) -> Unit): ENTITY = runAction("Save", dtoClass.entityType.kType) {
        val newEntity = entityModel.new {
            block.invoke(this)
        }
        newEntity
    }

    fun update(entityId: Long, updateFn: (newEntity: ENTITY) -> Unit): ENTITY?
        = runAction("Update", dtoClass.entityType.kType.withNullability(true)){
        val updated = pickById(entityId)?.let { picked ->
            updateFn.invoke(picked)
            picked
        } ?: run {
            warn("Update failed. Entity with id: ${entityId} for ${dtoClass.completeName} can not be found")
            null
        }
        updated
    }
}