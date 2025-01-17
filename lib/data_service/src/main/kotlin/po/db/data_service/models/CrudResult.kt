package po.db.data_service.models

import org.jetbrains.exposed.dao.LongEntity
import po.db.data_service.components.eventhandler.models.Event
import po.db.data_service.dto.interfaces.DataModel


interface OperationResult {
    fun isSuccess(): Boolean
}

abstract class AbstractOperationResult : OperationResult {
    override fun isSuccess(): Boolean = true
}


data class CrudResultSingle<DATA, ENTITY>(
    val dto: EntityDTO<DATA, ENTITY>,
    val event: Event?
): AbstractOperationResult() where DATA: DataModel, ENTITY : LongEntity

data class CrudResult<DATA, ENTITY>(
        val rootDTOs: List<EntityDTO<DATA, ENTITY>>,
        val event: Event?
) : AbstractOperationResult() where DATA: DataModel, ENTITY : LongEntity